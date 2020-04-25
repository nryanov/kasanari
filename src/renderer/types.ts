import * as Action from "./service/actionType"

export type Id = string;

export interface ConnectionSettings {
    readonly id: Id
    readonly user: string
    readonly host: string
    readonly database: string
    readonly password: string
    readonly port: number
    readonly name: string
}

export interface DatabaseInfo {
    // todo
    // todo: field - last update time?
}

export interface State {
    connectionSettings: {[key: string]: ConnectionSettings}
    activeConnections: {[key: string]: DatabaseInfo}
}

interface AddConnectionAction {
    type: typeof Action.ADD_CONNECTION
    connectionSettings: ConnectionSettings
}

interface RemoveConnectionAction {
    type: typeof Action.REMOVE_CONNECTION
    connectionId: Id
}

interface EditConnectionAction {
    type: typeof Action.EDIT_CONNECTION
    connectionSettings: ConnectionSettings
}

interface OpenConnectionAction {
    type: typeof Action.OPEN_CONNECTION
    connectionId: Id
}

interface CloseConnectionAction {
    type: typeof Action.CLOSE_CONNECTION
    connectionId: Id
}

export type AppAction = AddConnectionAction
    | RemoveConnectionAction
    | EditConnectionAction
    | OpenConnectionAction
    | CloseConnectionAction;
