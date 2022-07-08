import SUCCESS_SUFFIX from "redux-axios-middleware";
import HttpService from "../services/HttpService";
import UserService from "../services/UserService";
import {Action} from "redux";
import {AxiosRequestConfig, AxiosResponse} from "axios";

const LIST_TASKS = 'LIST_TASKS';
const ADD_TASK = 'ADD_TASK';
const DELETE_TASK = 'DELETE_TASK';

const BASE_URL = 'http://localhost:8080/api/v1'

export interface Task {
    id?: number,
    description: string,
    status: string
}

interface TaskRequestPayload {
    request: AxiosRequestConfig<Task>
}

type ListTasksSuccessPayload = AxiosResponse<Task[]>

interface DeleteTaskPayload {
    task: Task
}

interface TaskAction<P = any> extends Action<string> {
    payload: P
}

const tasksReducer = (state: Task[] = [], action: TaskAction) => {
    switch (action.type) {
        case LIST_TASKS + SUCCESS_SUFFIX:
            const taskListPayload = action.payload as ListTasksSuccessPayload
            return taskListPayload.data;

        case DELETE_TASK:
            const taskDeletePayload = action.payload as DeleteTaskPayload
            return state.filter((task) => task.id !== taskDeletePayload.task.id);

        default:
            return state;
    }
};

export default tasksReducer;

export const allTasks = (): TaskAction<TaskRequestPayload> => ({
    type: LIST_TASKS,
    payload: {
        request: {
            url: BASE_URL + '/item',
        },
    },
});

export const addTask = (task: Task): TaskAction<TaskRequestPayload> => {
    console.log(`${UserService.getUsername()} added the task ${task.description}`);
    return {
        type: ADD_TASK,
        payload: {
            request: {
                url: BASE_URL + '/item',
                method: HttpService.HttpMethods.POST,
                data: task,
            },
        },
    }
};

export const deleteTask = (task: Task): TaskAction => {
    console.log(`${UserService.getUsername()} deletes the task ${task.description}`);
    return {
        type: DELETE_TASK,
        payload: {
            task,
            request: {
                url: BASE_URL + `/item/${task.id}`,
                method: HttpService.HttpMethods.DELETE,
            },
        },
    }
};
