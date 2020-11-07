//
//  StringEncodingExtension.swift
//  react-native-bluetooth-classic
//
//  Created by Ken Davidson on 2020-11-06.
//

import Foundation

extension String.Encoding {
    
    /**
     * Annoyingly swift doesn't have a way of accessing the entires in an enum by name, which is mind
     * boggling coming from Java.  I don't particularly want to write out the switch statement for all the
     * possible encoding values in CFStringBuiltInEncodings and CFStringEncodings.  This solution
     * was provided in an Issue which was opened and resolved.
     *
     * The following will need to be provided as documentation
     * https://developer.apple.com/documentation/corefoundation/cfstringbuiltinencodings
     * https://developer.apple.com/documentation/corefoundation/cfstring/external_string_encodings
     *
     * From the documentation this will default to kCFStringEncodingInvalidId which could blow
     * things up completely.
     */
    static func from(_ charset: UInt32) -> String.Encoding {
        return String.Encoding(rawValue: CFStringConvertEncodingToNSStringEncoding(charset))
    }
}
