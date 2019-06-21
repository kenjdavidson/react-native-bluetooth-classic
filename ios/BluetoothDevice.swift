//
//  BluetoothDevice.swift
//  RNBluetoothClassic
//
//  Created by Ken Davidson on 2019-06-17.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation
import ExternalAccessory

/**
 Allows for the receiving of data to be handled externally.  The onReceivedData function
 is called providing the device and received data.  The delegate is responsible for
 parsing the data, determining what it wants to keep, and then returning the remaining
 string to which new data will be appended.
 */
protocol BluetoothRecievedDelegate {
    func onReceivedData(fromDevice:BluetoothDevice, receivedData:Data) -> Data
}

/**
 Provides overview and access functionality to a Bluetooth device - backed by an EAAccessory.  The
 BluetoothDevice is responsible for maintaining the connection Stream(s) with the device, using
 a provided StreamDelegate (generally provided by the module) to determine what to actually do
 with the data.
 
 When connecting it's possible to set maxBytesPerSend (512) and maxBytesPerReceive (1028) to provide
 some custom read/write functionality based on the specific device.
 
 A BluetoothReceivedDelegate can be assigned, in which case all the data read will be transfered
 to the delegate instead of storing it within the BluetoothDevice buffer.
 */
class BluetoothDevice: NSObject, StreamDelegate {
    
    static let MAX_BYTES_PER_SEND = 512
    static let MAX_BYTES_PER_RECEIVE = 1028
    
    private var session:EASession?
    public private(set) var accessory:EAAccessory
    
    private var inBuffer:Data
    private var outBuffer:Data
    private var maxBytesPerSend:Int = MAX_BYTES_PER_SEND
    private var maxBytesPerReceive:Int = MAX_BYTES_PER_RECEIVE
    
    /**
     Bluetooth received delegate used for handling the parsing of data during automation.  When
     this does not exist, the receivedData is updated directly.
     */
    var receivedDelegate:BluetoothRecievedDelegate?
    
    /**
     Initialize the BluetoothDevice with an EAAccessory.
     
     - parameter _: the accessory backing the BluetoothDevice
     */
    public init(_ accessory: EAAccessory) {
        self.accessory = accessory
        self.inBuffer = Data()
        self.outBuffer = Data()
    }
    
    /**
     Provides a printable/parciable NSDictionary which can be sent to the React bridge.
     When attempting to send the EAAccessory directly, a null is recieved on the JS side
     as the bridge JSON parser doesn't allow for some of its content.
     */
    func asDictionary() -> NSDictionary {
        let dict: NSDictionary = NSMutableDictionary()
        dict.setValue(accessory.name, forKey: "name")
        dict.setValue(accessory.serialNumber, forKey: "address")
        dict.setValue(accessory.serialNumber, forKey: "id")
        
        // Extra IOS specific details
        dict.setValue(accessory.modelNumber, forKey: "modelNumber")
        dict.setValue(accessory.protocolStrings, forKey: "protocols")
        dict.setValue(accessory.isConnected, forKey: "connected")
        return dict;
    }
    
    /**
     Attempts to connect to the EAAccessory using the provided protocol.  Since a device can
     only be connnected to using a single protocol once (but possible to connect to the same
     device using a different protocol) we need to allow this (albeit unlikely).
     
     The EASession is created and InputStream/OutputStream are configured with the appropriate
     delegate and scheduler - then finally opeend.
     
     Note - when attempting to use .current/.default for schedule things were not working.  Only
     after changing to .main/.common did the Stream events start firing.
     
     - parameter protocolString: in which use while connecting
     - parameter bytesPerSend: if overriding the default value of 512
     - parameter bytesPerRecieve: if overriding the default value of 1028
     */
    func connect(
        protocolString: String,
        bytesPerSend: Int,
        bytesPerReceive: Int
    ) {
        session = EASession(accessory: accessory, forProtocol: protocolString)
        
        if let currentSession = session {
            if let inStream = currentSession.inputStream, let outStream = currentSession.outputStream {
                inStream.delegate = self
                outStream.delegate = self
                inStream.schedule(in: .main, forMode: .common)
                outStream.schedule(in: .main, forMode: .common)
                inStream.open()
                outStream.open()
            }
        }
    }
    
    /**
     Attempts to connect using the default values.
     
     - parameter protocolString: protocol string to connect with
     */
    func connect(
        protocolString: String
    ) {
        connect(protocolString: protocolString,
                bytesPerSend: BluetoothDevice.MAX_BYTES_PER_SEND,
                bytesPerReceive: BluetoothDevice.MAX_BYTES_PER_RECEIVE)
    }
    
    /**
     Disconnects from the current session by closing the Input/Output Streams and
     niling out the session.
     */
    func disconnect() {
        if let currentSession = session {
            if let inStream = currentSession.inputStream {
                inStream.close()
                inStream.remove(from: .main, forMode: .common)
            }
            if let outStream = currentSession.outputStream{
                outStream.close()
                outStream.remove(from: .main, forMode: .common)
            }
        }
        
        session = nil
    }
    
    /**
     Delegate for EASession.InputStream.hasBytesAvailable - although generally won't be
     used while the StreamDelegate is running.
     
     - returns: true when bytes are available
     */
    @objc
    func hasBytesAvailable() -> Bool {
        return session?.inputStream!.hasBytesAvailable ?? false
    }
    
    /**
     Requests that the provided string be written to the EAAccessory.
     
     - parameter _: the String message to be written.
     */
    @objc
    func writeToDevice(_ message:String) {
        if let sending = message.data(using: .utf8) {
            outBuffer.append(sending)
        }
        
        // If there is space available for writing then we want to kick off the process.
        // If all the data cannot be fully written, then the hasSpaceAvailable will be
        // fired and we can continue.  In most cases, we shouldn't be sending that much
        // data.
        writeData()
    }
    
    /**
     Allows for manually reading from the device.
     
     - returns: the complete data available on the device
     */
    @objc
    func readFromDevice() -> String? {
        return readFromDevice(withDelimiter: nil)
    }
    
    /**
     Allows for manually reading from the device - where a delimiter is provided.  If a nil
     delimiter is provided then the entire string is returned and cleared.
     
     - parameter withDelimiter: the delimiter which we want to read until
     - returns: the available data up to the provided delimiter
     */
    func readFromDevice(withDelimiter delimiter:String?) -> String? {
        let content = String(data: inBuffer, encoding: .utf8)!
        let lookTo = delimiter != nil ? content.index(of: delimiter!) ?? content.endIndex : content.endIndex
        let message = String(content[..<lookTo])
        inBuffer = String(content[lookTo...]).data(using: .utf8) ?? Data()
        
        return message
    }
    
    /**
     StreamDelegate -
     handles in/out stream events
     */
    @objc
    func stream(
        _ aStream: Stream,
        handle eventCode: Stream.Event
    ) {
        switch(eventCode) {
        case .openCompleted:
            NSLog("Stream %@ has completed openning", aStream)
            Thread.sleep(forTimeInterval: 1.0) // Pause for connection
            break;
        case .hasBytesAvailable:
            NSLog("Stream %@ has bytes available", aStream)
            readData()
            break;
        case .hasSpaceAvailable:
            // As per the documents, this event occurs repeatedly as long as you're writing data
            // in the examples I've found they just assume the initial write will work (using
            // a smaller value) and then continues on doing so.
            NSLog("Stream %@ has space available", aStream)
            writeData()
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
     Reads data from the session.inputStream when there are available bytes.  Data is
     appended to the receivedData string for access later.  If there is a
     BluetoothDevice.onDataRead listener, then it will be notified instead of appending
     to the message string.
     */
    private func readData() {
        // Create the buffer that we'll read into
        let buffer = UnsafeMutablePointer<UInt8>.allocate(capacity: maxBytesPerReceive)
        let numBytesRead = session?.inputStream!.read(buffer, maxLength: maxBytesPerReceive) ?? 0
        
        if (numBytesRead <= 0) {
            NSLog("(BluetoothDevice:readData) No buffer")
            return
        }

        // If there is a receiveDelegate then let them deal with the data and update with the remaining
        inBuffer.append(buffer, count: numBytesRead)
        if let bd = receivedDelegate {            
            inBuffer = bd.onReceivedData(fromDevice: self, receivedData: inBuffer)
        }
    }
    
    /**
     Attempts to write as much data to the OutputStream as possible - currently this is maxed out at 512
     bytes per attempt.  If all the data can't be written at one time, then the remaining will be written
     at the next hasSpaceAvailable event.
     */
    private func writeData() {
        if (outBuffer.isEmpty) {
            NSLog("(BluetoothDevice:writeData) No buffer data scheduled for deliver")
            return
        }
        
        let len:Int = (outBuffer.count > maxBytesPerSend) ? maxBytesPerSend : outBuffer.count
        NSLog("(BluetoothDevice:writeData) Attempting to send %d bytes to the device", len)
        
        let buffer:UnsafeMutablePointer<UInt8> = UnsafeMutablePointer.allocate(capacity: len)
        outBuffer.copyBytes(to: buffer, count: len)
        outBuffer.removeFirst(len)
        
        let bytesWritten = session?.outputStream!.write(buffer, maxLength: len) ?? 0
        NSLog("(BluetoothDevice:writeData) Sent %d bytes to the device", bytesWritten)
    }
    
    @objc
    func clear() {
        inBuffer.removeAll()
        outBuffer.removeAll()
    }
}
