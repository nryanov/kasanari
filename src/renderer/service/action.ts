import {ActionCreator} from 'redux';
import * as Action from "./actionType"
import {AppAction, ConnectionSettings, Id} from "../types"

export const addConnection: ActionCreator<AppAction> = (connectionSettings: ConnectionSettings) => {
    return {
        type: Action.ADD_CONNECTION,
        connectionSettings
    }
};

export const removeConnection: ActionCreator<AppAction> = (connectionId: Id) => {
    return {
        type: Action.REMOVE_CONNECTION,
        connectionId
    }
};

export const editConnection: ActionCreator<AppAction> = (connectionSettings: ConnectionSettings) => {
    return {
        type: Action.EDIT_CONNECTION,
        connectionSettings
    }
};

export const openConnection: ActionCreator<AppAction> = (connectionId: Id) => {
    return {
        type: Action.OPEN_CONNECTION,
        connectionId
    }
};

export const closeConnection: ActionCreator<AppAction> = (connectionId: Id) => {
    return {
        type: Action.CLOSE_CONNECTION,
        connectionId
    }
};
