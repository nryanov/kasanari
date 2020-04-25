import {Reducer} from "redux";
import _ from "lodash"
import * as Action from "./actionType";
import {AppAction, State} from "../types";

export const reducer: Reducer<State, AppAction> = (state: State, action: AppAction): State => {
    switch (action.type) {
        case Action.ADD_CONNECTION:
            return {
            ...state,
            connectionSettings: _.set({...state.connectionSettings}, action.connectionSettings.id, action.connectionSettings)
            };
        case Action.REMOVE_CONNECTION:
            return {
                ...state,
                connectionSettings: _.pickBy(state.connectionSettings, (val, key) => key !== action.connectionId),
                activeConnections: _.pickBy(state.activeConnections, (val, key) => key !== action.connectionId)
            };
        case Action.EDIT_CONNECTION:
            return {
                ...state,
                connectionSettings: _.set({...state.connectionSettings}, action.connectionSettings.id, action.connectionSettings)
            };
        case Action.OPEN_CONNECTION:
            return {
                ...state,
                activeConnections: _.set({...state.activeConnections}, action.connectionId, {})
            };
        case Action.CLOSE_CONNECTION:
            return {
                ...state,
                activeConnections: _.pickBy(state.activeConnections, (val, key) => key !== action.connectionId)
            };
        default: return state;
    }
};
