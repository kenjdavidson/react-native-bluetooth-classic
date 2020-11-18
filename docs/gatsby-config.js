module.exports = {
  pathPrefix: '/react-native-bluetooth-classic',
  plugins: [
    {
      resolve: 'gatsby-theme-apollo-docs',
      options: {
        root: __dirname,
        siteName: 'React Native Bluetooth Classic',
        description: "Communicate with Bluetooth Classic devices on Android and IOS",
        defaultVersion: '1.0.0-rc.1',
        logoLink: '/react-native-bluetooth-classic',
        sidebarCategories: {
          null: [
            'index',
            'api-overview'
          ],
          'React Native': [
            'react-native/rn-bluetooth-classic',
            'react-native/rn-bluetooth-device'
          ],
          'Android': [
            'android/index',
            'android/rn-bluetooth-classic-package',
            'android/rn-bluetooth-classic-module',
            'android/connection-acceptor',
            'android/connection-connector',
            'android/device-connection'
          ],
          'IOS': [
            'ios/index'
          ],
          'Guides': [
            'guides/hex-encoding-decoding'
          ]
        }
      }
    },
    {
      resolve: `gatsby-plugin-typography`,
      options: {
        pathToConfigModule: `src/utils/typography`,
      },
    },

  ]
};
