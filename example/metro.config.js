const path = require("path");
const { getDefaultConfig, mergeConfig } = require("@react-native/metro-config");

const defaultConfig = getDefaultConfig(__dirname);
const { resolver: { sourceExts, assetExts } } = defaultConfig;

/**
 * Metro configuration
 * https://facebook.github.io/metro/docs/configuration
 *
 * @type {import('metro-config').MetroConfig}
 */
const config = {
  transformer: {
    getTransformOptions: async () => ({
      transform: {
        experimentalImportSupport: false,
        inlineRequires: true,
      },
    }),
  },
  resolver: {
    assetExts: assetExts.filter((ext) => ext !== "svg"),
    sourceExts: [...sourceExts, "svg"],
    resolverMainFields: ["sbmodern", "react-native", "browser", "main"],
  },
  watchFolders: [path.resolve(__dirname, "../")],
};

module.exports = mergeConfig(defaultConfig, config);


// const path = require('path');
// const blacklist = require('metro-config/src/defaults/exclusionList');
// const escape = require('escape-string-regexp');
// const pak = require('../package.json');
//
// const root = path.resolve(__dirname, '..');
//
// const modules = Object.keys({
//   ...pak.peerDependencies,
// });
//
// module.exports = {
//   projectRoot: __dirname,
//   watchFolders: [root],
//
//   // We need to make sure that only one version is loaded for peerDependencies
//   // So we blacklist them at the root, and alias them to the versions in example's node_modules
//   resolver: {
//     blacklistRE: blacklist(
//       modules.map(
//         (m) =>
//           new RegExp(`^${escape(path.join(root, 'node_modules', m))}\\/.*$`)
//       )
//     ),
//
//     extraNodeModules: modules.reduce((acc, name) => {
//       acc[name] = path.join(__dirname, 'node_modules', name);
//       return acc;
//     }, {}),
//   },
//
//   transformer: {
//     getTransformOptions: async () => ({
//       transform: {
//         experimentalImportSupport: false,
//         inlineRequires: true,
//       },
//     }),
//   },
// };
