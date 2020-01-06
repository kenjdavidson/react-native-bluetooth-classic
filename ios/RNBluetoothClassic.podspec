
Pod::Spec.new do |s|
  s.name         = "RNBluetoothClassic"
  s.version      = "1.0.0"
  s.summary      = "RNBluetoothClassic"
  s.description  = <<-DESC
                  Bluetooth classic support for React Native iOS through the External Accessory library.
                   DESC
  s.license      = { :type => "MIT", :file => "LICENSE" }
  s.author       = { "author" => "ken.j.davidson@live.ca" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/kenjdavidson/react-native-bluetooth-classic.git", :tag => "v#{s.version}" }
  s.source_files = "RNBluetoothClassic/**/*.{h,m}"
  s.requires_arc = true
  s.homepage = "https://github.com/kenjdavidson/react-native-bluetooth-classic"

  s.dependency "React"
  #s.dependency "others"

end