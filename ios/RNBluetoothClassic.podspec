
Pod::Spec.new do |s|
  s.name         = "RNBluetoothClassic"
  s.version      = "1.0.0"
  s.summary      = "RNBluetoothClassic"
  s.description  = <<-DESC
                  RNBluetoothClassic
                   DESC
  s.homepage     = ""
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.com" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/author/react-native-bluetooth-classic.git", :tag => "master" }
  s.source_files  = "RNBluetoothClassic/**/*.{h,m}"
  s.requires_arc = true


  s.dependency "React"
  #s.dependency "others"

end

  