/**
 * Metro configuration for React Native
 * https://github.com/facebook/react-native
 *
 * @format
 */

// Original file contents
// module.exports = {
//   transformer: {
//     getTransformOptions: async () => ({
//       transform: {
//         experimentalImportSupport: false,
//         inlineRequires: false,
//       },
//     }),
//   },
// };

// Resolution decribed in the following comment, which as the same configuration with folder
// strcuture as this.
// https://github.com/facebook/metro/issues/1#issuecomment-501143843
let path = require('path');
module.exports = {
    transformer: {
        getTransformOptions: async () => ({
            transform: {
                experimentalImportSupport: false,
                inlineRequires: false
            }
        })
    },
    resolver: {
        /* This configuration allows you to build React-Native modules and
         * test them without having to publish the module. Any exports provided
         * by your source should be added to the "target" parameter. Any import
         * not matched by a key in target will have to be located in the embedded
         * app's node_modules directory.
         */
        extraNodeModules: new Proxy(
            /* The first argument to the Proxy constructor is passed as 
             * "target" to the "get" method below.
             * Put the names of the libraries included in your reusable
             * module as they would be imported when the module is actually used.
             */
            {
                'react-native-bluetooth-classic': path.resolve(__dirname, '../')
            },
            {
                get: (target, name) =>
                {
                    if (target.hasOwnProperty(name))
                    {
                        return target[name];
                    }
                    return path.join(process.cwd(), `node_modules/${name}`);
                }
            }
        )
    },
    projectRoot: path.resolve(__dirname),
    watchFolders: [
        path.resolve(__dirname, '../')
    ]
};

// Added as per https://github.com/facebook/metro/issues/1#issuecomment-421628147
// in order to resolve the issue with symlinked npm dependancies
// After testing this doesn't work with this specific case - due to the folder structure
// being react-native-module and react-native-module/example contain the same dependancies
// there is a name collision.

// const fs = require('fs')
// const getDevPaths = require('get-dev-paths')
// const projectRoot = __dirname
// module.exports = {
//   // Old way
//   getProjectRoots: () => Array.from(new Set(
//     getDevPaths(projectRoot).map($ => fs.realpathSync($))
//   )),
//   // New way
//   watchFolders: Array.from(new Set(
//     getDevPaths(projectRoot).map($ => fs.realpathSync($))
//   ))
// }
