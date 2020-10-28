module.exports = {
  pathPrefix: '/react-native-bluetooth-classic',
  plugins: [
    {
      resolve: 'gatsby-theme-apollo-docs',
      options: {
        root: __dirname,
        siteName: 'React Native Bluetooth Classic',
        description: "Communicate with Bluetooth Classic devices on Android and IOS",
        sidebarCategories: {
          null: [
            'index',
            'api-overview'
          ],
          'React Native': [
            'react-native/index',
            'react-native/rn-bluetooth-classic',
            'react-native/rn-bluetooth-device'
          ],
          'Android': [
            'android/index'
          ],
          'IOS': [
            'ios/index'
          ]
        }
      }
    }
  ]
};
