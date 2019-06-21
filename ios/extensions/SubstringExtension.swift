//
//  SubstringExtension.swift
//  RNBluetoothClassic
//
//  Created by Ken Davidson on 2019-06-20.
//  Copyright © 2019 Facebook. All rights reserved.
//
// https://stackoverflow.com/questions/32305891/index-of-a-substring-in-a-string-with-swift/32306142
//
// 
//

import Foundation

extension StringProtocol {
    
    /**
     * Returns the first index where a specified value appears in the collection.
     * - parameter of: String for which to search
     * - parameter options: options for the requested searc
     */
    func index(of string: Self, options: String.CompareOptions = []) -> Index? {
        return range(of: string, options: options)?.lowerBound
    }
    
    /**
     * Returns the last index where a specified value appears in the collection.
     * - parameter of: String for which to search
     * - parameter options: options for the requested searc
     */
    func endIndex(of string: Self, options: String.CompareOptions = []) -> Index? {
        return range(of: string, options: options)?.upperBound
    }
    
    /**
     * Returns an array on indices where a specified value appears
     * - parameter of: String for which to search
     * - parameter options: options for the requested searc
     */
    func indexes(of string: Self, options: String.CompareOptions = []) -> [Index] {
        var result: [Index] = []
        var startIndex = self.startIndex
        while startIndex < endIndex,
            let range = self[startIndex...].range(of: string, options: options) {
                result.append(range.lowerBound)
                startIndex = range.lowerBound < range.upperBound ? range.upperBound :
                    index(range.lowerBound, offsetBy: 1, limitedBy: endIndex) ?? endIndex
        }
        return result
    }
    
    /**
     * Returns an array of ranges where a specified value occurs.
     * - parameter of: String for which to search
     * - parameter options: options for comparing
     */
    func ranges(of string: Self, options: String.CompareOptions = []) -> [Range<Index>] {
        var result: [Range<Index>] = []
        var startIndex = self.startIndex
        while startIndex < endIndex,
            let range = self[startIndex...].range(of: string, options: options) {
                result.append(range)
                startIndex = range.lowerBound < range.upperBound ? range.upperBound :
                    index(range.lowerBound, offsetBy: 1, limitedBy: endIndex) ?? endIndex
        }
        return result
    }
}
