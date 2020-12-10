module.exports = {
  "transform": {
    "^.+\\.tsx?$": "ts-jest"
  },
  "globals": {
    "ts-jest": {
      "tsconfig": "tsconfig.jest.json",
      "compiler": "ttypescript"
    }
  },
  "testRegex": "(/__tests__/.*|(\\.|/)(test|spec))\\.(jsx?|tsx?)$",
  "moduleFileExtensions": [
    "ts",
    "tsx",
    "js",
    "jsx",
    "json",
    "node"
  ],
  "modulePaths": [
    "<rootDir>"
  ],
  "modulePathIgnorePatterns": [
    "<rootDir>/node_modules/react-native/"
  ],
};