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
 *@author kendavidson
 */
class DelimitedStringDeviceConnectionImpl : NSObject, DeviceConnection, StreamDelegate {
 
    var dataReceivedDelegate: DataReceivedDelegate?
    
    private var session: EASession?
    private var inBuffer: Data
    private var outBuffer: Data
    
    private(set) var accessory: EAAccessory
    private(set) var properties: NSDictionary
    
    private var readSize: Int
    private var delimiter: String
    private var encoding: String.Encoding
    
    init(
        accessory: EAAccessory,
        properties: NSDictionary
    ) {
        self.accessory = accessory;
        self.properties = NSDictionary(dictionary: properties)
        self.inBuffer = Data()
        self.outBuffer = Data()
        
        self.readSize = (self.properties.value(forKey: "read_size") != nil)
            ? self.properties.value(forKey: "read_size") as! Int
            : 1024
        
        self.delimiter = (self.properties.value(forKey: "delimiter") != nil)
            ? self.properties.value(forKey: "delimiter") as! String
            : "\n"
        
        let stringEncoding = (self.properties.value(forKey: "charset") != nil)
            ? self.properties.value(forKey: "charset") as! CFStringEncoding
            : CFStringBuiltInEncodings.ASCII.rawValue
        self.encoding = String.Encoding.from(stringEncoding)
        
    }
    
    /**
     * This implementation attempts to open an EASession for the provided protocol string
     */
    func connect() {
        let protocolString: String = self.properties.value(forKey: "protocol_string") as! String
        
        NSLog("(BluetoothDevice:connect) Attempting Bluetooth connection to %@ using protocol %@", accessory.serialNumber, protocolString)
        session = EASession(accessory: accessory, forProtocol: protocolString)
        
        if let currentSession = session {
            if let inStream = currentSession.inputStream, let outStream = currentSession.outputStream {
                inStream.delegate = self
                outStream.delegate = self
                inStream.schedule(in: .main, forMode: .commonModes)
                outStream.schedule(in: .main, forMode: .commonModes)
                inStream.open()
                outStream.open()
            }
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
     */
    func write(_ message: String) {
        NSLog("(BluetoothDevice:writeToDevice) Writing %@ to device %@", message, accessory.serialNumber)
        if let sending = message.data(using: self.encoding) {
            outBuffer.append(sending)
        }
        
        // If there is space available for writing then we want to kick off the process.
        // If all the data cannot be fully written, then the hasSpaceAvailable will be
        // fired and we can continue.  In most cases, we shouldn't be sending that much
        // data.
        writeData((session?.outputStream)!)
    }
    
    /**
     * Reads a single message from the device, using the provided delimiter.  This is done by pulling out from the
     * start to the first instance of the delmiter, then
     */
    func read() -> String? {
        NSLog("(BluetoothDevice:readFromDevice) Reading device %@ until delimiter %@",
              self.accessory.serialNumber, self.delimiter)
        let content = String(data: inBuffer, encoding: self.encoding)!
        var message:String?
        
        if let lookTo = content.index(of: self.delimiter) {
            message = String(content[..<lookTo])
            inBuffer = String(content[lookTo...]).data(using: self.encoding) ?? Data()
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
            readData(aStream as! InputStream)
            break;
        case .hasSpaceAvailable:
            // As per the documents, this event occurs repeatedly as long as you're writing data
            // in the examples I've found they just assume the initial write will work (using
            // a smaller value) and then continues on doing so.
            NSLog("Stream %@ has space available", aStream)
            writeData(aStream as! OutputStream)
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
    private func readData(_ stream: InputStream) {
        let buffer = UnsafeMutablePointer<UInt8>.allocate(capacity: readSize)
        
        while (stream.hasBytesAvailable) {
            let numBytesRead = stream.read(buffer, maxLength: readSize)
            
            if (numBytesRead < 0) {
                break;
            }
            
            inBuffer.append(buffer, count: numBytesRead)
        }
    
        if let delegate = self.dataReceivedDelegate {
            let count = available()
            while (count > 0) {
                if let message = read() {
                    let data =
                    delegate.onReceivedData(fromDevice:  accessory, receivedData: message)
                }
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
    private func writeData(_ stream: OutputStream) {
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
