module.exports = {
    roots: [
        "src"
    ],
    transform: {
        "^.+\\.ts$": "ts-jest"
    },
    moduleNameMapper: {
        underscore$: 'lodash',
    },
    // automock: false,
    // testEnvironment: "node"
};
