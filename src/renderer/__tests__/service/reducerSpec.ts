import * as Action from "../../service/actionType"
import {AppAction, ConnectionSettings, State} from "../../types"
import {reducer} from "../../service/reducer"

function initialState(): State {
    return {
        connectionSettings: {},
        activeConnections: {}
    }
}

function connectionSettingsStub(): ConnectionSettings {
    return {
        id: "id",
        user: "username",
        host: "host",
        database: "db",
        password: "pass",
        port: 5432,
        name: "test"
    }
}

describe("reducer spec", () => {
    it("ADD_CONNECTION", () => {
        const state = initialState();
        const connectionSettings = connectionSettingsStub();
        const action: AppAction = {
            type: Action.ADD_CONNECTION,
            connectionSettings: connectionSettings
        };
        const expectedState = {
            ...state,
            connectionSettings: {"id": connectionSettings}
        };

        expect(reducer(state, action)).toEqual(expectedState);
    });

    it("REMOVE_CONNECTION", () => {
        const connectionSettings = connectionSettingsStub();
        const state = {
            ...initialState(),
            connectionSettings: {"id": connectionSettings}
        };
        const action: AppAction = {
            type: Action.REMOVE_CONNECTION,
            connectionId: "id"
        };
        const expectedState = {
            ...state,
            connectionSettings: {}
        };

        expect(reducer(state, action)).toEqual(expectedState);
    });

    it("EDIT_CONNECTION", () => {
        const connectionSettings = connectionSettingsStub();
        const state = {
            ...initialState(),
            connectionSettings: {"id": connectionSettings}
        };
        const updatedConnectionSettings = {
            ...connectionSettings,
            name: "updated"
        };
        const action: AppAction = {
            type: Action.EDIT_CONNECTION,
            connectionSettings: updatedConnectionSettings
        };
        const expectedState = {
            ...state,
            connectionSettings: {"id": updatedConnectionSettings}
        };

        expect(reducer(state, action)).toEqual(expectedState);
    });

    it("OPEN_CONNECTION", () => {
        const state = initialState();
        const action: AppAction = {
            type: Action.OPEN_CONNECTION,
            connectionId: "id"
        };
        const expectedState = {
            ...state,
            activeConnections: {"id": {}}
        };

        expect(reducer(state, action)).toEqual(expectedState);
    });

    it("CLOSE_CONNECTION", () => {
        const state: State = {
            ...initialState(),
            activeConnections: {"id": {}}
        };
        const action: AppAction = {
            type: Action.CLOSE_CONNECTION,
            connectionId: "id"
        };
        const expectedState = {
            ...state,
            activeConnections: {}
        };

        expect(reducer(state, action)).toEqual(expectedState);
    });
});
