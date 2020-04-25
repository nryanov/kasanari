const path = require("path");

const NODE_ENV = process.env.NODE_ENV;

module.exports = {
    mode: NODE_ENV,
    target: 'electron-renderer',
    entry: {
        renderer: path.join(__dirname, "src/renderer/index.tsx")
    },
    output: {
        path: path.join(__dirname, "dist"),
        filename: "renderer.js"
    },
    module: {
        rules: [
            {
                exclude: /node_modules/,
                test: /\.tsx?$/,
                use: "ts-loader"
            },
            {
                test: /\.node$/,
                use: {
                    loader: 'node-loader',
                },
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