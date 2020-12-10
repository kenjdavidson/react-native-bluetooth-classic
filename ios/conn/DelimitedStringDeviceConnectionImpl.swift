//
//  DelimitedStringDeviceConnectionImpl.swift
//  react-native-bluetooth-classic
//
//  Created by Ken Davidson on 2020-11-06.
//

import Foundation
import ExternalAccessory

/**
 * Provides connection to a device which communicates through string delimited messages.  This connection uses the
 * properties:
 * - read_size specifying the read and write max sizes, defaults to 1024
 * - delimier used to split content into messages, defaults to "\n"
 * - charset used to determine the String.Encoding, defaults to ASCII
 * Note that charset in IOS is a UInt32, unlike Java, which will need to somehow be managed on the Javascript side
 * to make life easier.
 *
 * @author kendavidson
 */
class DelimitedStringDeviceConnectionImpl : NSObject, DeviceConnection, StreamDelegate {
 
    private var _dataReceivedDelegate: DataReceivedDelegate?
    var dataReceivedDelegate: DataReceivedDelegate? {
        set(newDelegate) {
            if let unwrapped = newDelegate {
                while let data = read() {
                    unwrapped.onReceivedData(fromDevice:  accessory, receivedData: data)
                }
            }
            self._dataReceivedDelegate = newDelegate
        }
        get {
            return self._dataReceivedDelegate
        }
    }
    
    private var session: EASession?
    private var inBuffer: Data
    private var outBuffer: Data
    
    private(set) var accessory: EAAccessory
    private(set) var properties: Dictionary<String,Any>
    
    private var readSize: Int
    private var delimiter: String
    private var encoding: String.Encoding
    
    init(
        accessory: EAAccessory,
        options: Dictionary<String,Any>
    ) {
        self.accessory = accessory;
        self.properties = Dictionary<String,Any>()
        self.properties.merge(options) { $1 }
        
        self.inBuffer = Data()
        self.outBuffer = Data()
        
        // For lack of knowing how to actually do this properly, Swift (I can't stand) doesn't like
        // the enumeration method from Java.  If someone can figure this one out, let me know.
        if let value = self.properties["READ_SIZE"] { self.readSize = value as! Int }
        if let value = self.properties["read_size"] { self.readSize = value as! Int }
        else { self.readSize = 1024 }
        
        if let value = self.properties["DELIMITER"] { self.delimiter = value as! String }
        else if let value = self.properties["delimiter"] { self.delimiter = value as! String }
        else { self.delimiter = "\n" }
        
        if let value = self.properties["DEVICE_CHARSET"] { self.encoding = String.Encoding.from(value as! CFStringEncoding) }
        else if let value = self.properties["device_charset"] { self.encoding = String.Encoding.from(value as! CFStringEncoding) }
        else if let value = self.properties["charset"] { self.encoding = String.Encoding.from(value as! CFStringEncoding) }
        else { self.encoding = String.Encoding.from(CFStringBuiltInEncodings.ASCII.rawValue) }
    }
    
    /**
     * This implementation attempts to open an EASession for the provided protocol string
     */
    func connect() throws {
        let protocolString: String = self.properties["PROTOCOL_STRING"] as! String
        
        NSLog("(BluetoothDevice:connect) Attempting Bluetooth connection to %@ using protocol %@", accessory.serialNumber, protocolString)
        if let connected = EASession(accessory: accessory, forProtocol: protocolString) {
            self.session = connected
            
            if let inStream = connected.inputStream,
                let outStream = connected.outputStream {
                inStream.delegate = self
                outStream.delegate = self
                inStream.schedule(in: .main, forMode: .commonModes)
                outStream.schedule(in: .main, forMode: .commonModes)
                inStream.open()
                outStream.open()
            }
        } else {
            throw BluetoothError.CONNECTION_FAILED            
        }
    }
    
    /**
     * Attempts to disconnect from the EAAccessory and EASession
     */
    func disconnect() {
        NSLog("(BluetoothDevice:disconnect) Attempting disconnect from devices %@", accessory.serialNumber)
        if let currentSession = session {
            if let inStream = currentSession.inputStream {
                inStream.close()
                inStream.remove(from: .main, forMode: .commonModes)
            }
            if let outStream = currentSession.outputStream{
                outStream.close()
                outStream.remove(from: .main, forMode: .commonModes)
            }
        }
        
        session = nil
    }
    
    /**
     * Returns the number of mesages available.  As this is a delmited string connection, the number of messages
     * are the number of delimiters found.
     */
    func available() -> Int {
        var count = 0;
        
        let content = String(data: inBuffer, encoding: self.encoding)!
        while (content.index(of: delimiter) != nil) {
            count += 1
        }
        
        return count;
    }
    
    /**
     * Attempts to write to the out buffer.  This intermedate step is required so that the StreamDelegate will find and read
     * the available information when more space is available on the stream.
     * - parameter message: the encoded message which will be written
     */
    func write(_ data: Data) -> Bool {
        if let sending = String(data: data, encoding: self.encoding) {
            NSLog("(BluetoothDevice:writeToDevice) Writing %@ to device %@", sending, accessory.serialNumber)
            outBuffer.append(data)
            
            // If there is space available for writing then we want to kick off the process.
            // If all the data cannot be fully written, then the hasSpaceAvailable will be
            // fired and we can continue.  In most cases, we shouldn't be sending that much
            // data.
            writeDataToStream((session?.outputStream)!)
        } else {
            return false
        }
        
        return true
    }
    
    /**
     * Reads the next message from the inBuffer.  This particular implementation converse the inBuffer into a
     * String using the provided encoding, then search for the first instance of that delimiter, and finally
     * updates the inBuffer with the remaining data
     */
    func read() -> String? {
        NSLog("(BluetoothDevice:readFromDevice) Reading device %@ until delimiter %@",
              self.accessory.serialNumber, self.delimiter)
        
        let content = String(data: inBuffer, encoding: self.encoding)!
        var message:String?

        if let index = content.index(of: self.delimiter) {
            message = String(content[..<index])
            inBuffer = String(content[content.index(after: index)...])
                .data(using: self.encoding) ?? Data()
        }
        
        return message
    }
    
    /**
     * Removed all data from the in/out buffers. 
     */
    func clear() {
        inBuffer.removeAll()
        outBuffer.removeAll()
    }
    
    /**
     * Implements the StreamDelegate stream method.
     */
    @objc
    func stream(
        _ aStream: Stream,
        handle eventCode: Stream.Event
    ) {
        switch(eventCode) {
        case .openCompleted:
            NSLog("Stream %@ has completed openning", aStream)
            Thread.sleep(forTimeInterval: 0.5) // Pause for connection
            break;
        case .hasBytesAvailable:
            NSLog("Stream %@ has bytes available", aStream)
            readDataFromStream(aStream as! InputStream)
            break;
        case .hasSpaceAvailable:
            // As per the documents, this event occurs repeatedly as long as you're writing data
            // in the examples I've found they just assume the initial write will work (using
            // a smaller value) and then continues on doing so.
            NSLog("Stream %@ has space available", aStream)
            writeDataToStream(aStream as! OutputStream)
            break;
        case .errorOccurred:
            NSLog("Stream %@ has had an error occur", aStream)
            break;
        case .endEncountered:
            NSLog("Stream %@ has encounted the end", aStream)
            break;
        default:
            NSLog("Stream %@ had some other event occur", aStream)
        }
    }
    
    /**
     * Reads data from the session.inputStream when there are available bytes.  Data is
     * appended to the receivedData string for access later.  If there is a
     * BluetoothDevice.onDataRead listener, then it will be notified instead of appending
     * to the message string.
     */
    private func readDataFromStream(_ stream: InputStream) {
        let buffer = UnsafeMutablePointer<UInt8>.allocate(capacity: readSize)
        
        while (stream.hasBytesAvailable) {
            let numBytesRead = stream.read(buffer, maxLength: readSize)
            
            if (numBytesRead < 0) {
                break;
            }
            
            inBuffer.append(buffer, count: numBytesRead)
        }
    
        if let delegate = self.dataReceivedDelegate {
            while let data = read() {
                delegate.onReceivedData(fromDevice:  accessory, receivedData: data)
            }
        }
    }
    
    /**
     * Attempts to write as much data to the OutputStream as possible - currently this is maxed out at 512
     * bytes per attempt.  If all the data can't be written at one time, then the remaining will be written
     * at the next hasSpaceAvailable event.
     *
     * Messages are only written to the outBuffer based on the size available or the readSize, this should probably
     * just write what is there
     */
    private func writeDataToStream(_ stream: OutputStream) {
        if (outBuffer.isEmpty) {
            NSLog("(BluetoothDevice:writeData) No buffer data scheduled for deliver")
            return
        }
        
        let len:Int = (outBuffer.count > self.readSize) ? self.readSize : outBuffer.count
        NSLog("(BluetoothDevice:writeData) Attempting to send %d bytes to the device", len)
        
        let buffer:UnsafeMutablePointer<UInt8> = UnsafeMutablePointer.allocate(capacity: len)
        outBuffer.copyBytes(to: buffer, count: len)
        outBuffer.removeFirst(len)
        
        let bytesWritten = stream.write(buffer, maxLength: len)
        NSLog("(BluetoothDevice:writeData) Sent %d bytes to the device", bytesWritten)
    }
}

class DelimitedStringDeviceConnectionFactory : DeviceConnectionFactory {
    func create(accessory: EAAccessory, options: Dictionary<String, Any>) -> DeviceConnection {
        return DelimitedStringDeviceConnectionImpl(accessory: accessory, options: options)
    }
}
