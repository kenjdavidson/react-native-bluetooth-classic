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
            'android/index'
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
