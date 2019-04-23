
Pod::Spec.new do |s|
  s.name         = "RNOwPay"
  s.version      = "1.0.0"
  s.summary      = "RNOwPay"
  s.description  = <<-DESC
                  RNOwPay
                   DESC
  s.homepage     = "http://www.openwater.com.cn"
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/author/RNOwPay.git", :tag => "master" }
  s.source_files  = "**/*.{h,m}"
  s.requires_arc = true


  s.dependency "React"
  s.resource = "lib/AlipaySDK/{*.bundle}"
  s.vendored_frameworks = "lib/AlipaySDK/{*.framework}"
  s.vendored_libraries = "lib/{**/*.a}"
  s.framework = "CoreMotion"
  #s.dependency "others"

end
