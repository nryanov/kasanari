const path = require("path");

const NODE_ENV = process.env.NODE_ENV;

module.exports = {
    mode: NODE_ENV,
    target: 'electron-main',
    entry: {
        index: path.join(__dirname, "src/main/index.ts"),
    },
    output: {
        path: path.join(__dirname, "dist"),
        filename: "main.js"
    },
    module: {
        rules: [
            {
                exclude: /node_modules/,
                test: /\.tsx?$/,
                use: "ts-loader"
            },
            {
                test: /\.css$/,
                use: ["style-loader", "css-loader"]
            }
        ]
    },
    resolve: {
        extensions: [".ts", ".tsx", ".js"]
    }
};