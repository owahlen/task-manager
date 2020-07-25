import http from './http-client'
import {Task} from "../models/task.model";
import {AxiosResponse} from "axios";
import {TaskDTO} from "../models/task-dto.model";

let taskPath = '/task'

class TaskService {
    async getAll(): Promise<AxiosResponse<Array<Task>>> {
        return await http.get(`${taskPath}`);
    }

    async get(id: number): Promise<AxiosResponse<Task>> {
        return await http.get(`${taskPath}/${id}`);
    }

    async create(taskDTO: TaskDTO): Promise<AxiosResponse<Task>> {
        return await http.post(`${taskPath}`, taskDTO);
    }

    async update(id: number, taskDTO: TaskDTO): Promise<AxiosResponse<Task>> {
        return await http.put(`${taskPath}/${id}`, taskDTO);
    }

    async delete(id: number): Promise<AxiosResponse<boolean>> {
        return await http.delete(`${taskPath}/${id}`);
    }

    async findByDescription(description: string): Promise<AxiosResponse<Array<Task>>> {
        return await http.get(`${taskPath}/search?description=${description}`);
    }

}

export default new TaskService();
