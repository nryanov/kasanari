import {app, BrowserWindow, screen } from "electron";
import path from "path"

const WIDTH_RATIO = 0.8;
const ASPECT_RATIO = 10 / 16;

function createWindow(): Promise<void> {
    const screenWidth = screen.getPrimaryDisplay().workAreaSize.width;

    let win = new BrowserWindow({
        width: screenWidth * WIDTH_RATIO,
        height: screenWidth * WIDTH_RATIO * ASPECT_RATIO,
        webPreferences: {
            nodeIntegration: true
        }
    });

    return win.loadFile(path.join(__dirname, "../template/index.html"))
}

app.whenReady().then(createWindow);