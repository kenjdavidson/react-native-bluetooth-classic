module.exports = {
  pathPrefix: '/react-native-bluetooth-classic',
  trailingSlash: 'always',
  siteMetadata: {
    siteTitle: `Bluetooth Classic Docs`,
    defaultTitle: `Bluetooth Classic Docs`,
    siteTitleShort: `RNBluetooth Docs`,
    siteDescription: `Documentation site for React Native Bluetooth Classic library`,
    siteUrl: `https://kenjdavidson.com/react-native-bluetooth-classic`,
    siteAuthor: `@kenjdavidson`,
    siteImage: `/banner.png`,
    siteLanguage: `en`,
    themeColor: `#8257E6`,
    basePath: ``,
  },
  plugins: [
    {
      resolve: `@rocketseat/gatsby-theme-docs`,
      options: {
        configPath: `src/config`,
        docsPath: `src/docs`,
        homePath: `src/home`,
        yamlFilesPath: `src/yamlFiles`,
        repositoryUrl: `https://github.com/jpedroschmitz/rocketdocs`,
        baseDir: `examples/gatsby-theme-docs`,
        gatsbyRemarkPlugins: [],
      },
    },
    {
      resolve: `gatsby-plugin-manifest`,
      options: {
        name: `React Native Bluetooth Classic`,
        short_name: `RN Bluetooth Classic`,
        start_url: `/`,
        background_color: `#ffffff`,
        display: `standalone`,
        icon: `static/favicon.png`,
      },
    },
    `gatsby-plugin-sitemap`,
    // {
    //   resolve: `gatsby-plugin-google-analytics`,
    //   options: {
    //     trackingId: `YOUR_ANALYTICS_ID`,
    //   },
    // },
    {
      resolve: `gatsby-plugin-canonical-urls`,
      options: {
        siteUrl: `https://kenjdavidson.com/react-native-bluetooth-classic`,
      },
    },
    `gatsby-plugin-offline`,
  ],
};
