import React, {Component} from "react";
import TaskService from "../services/task.service";
import {Task} from "../models/task.model";

interface State {
    currentTask: Task;
    message: string;
}

export default class TaskItem extends Component<any, State> {
    constructor(props: any) {
        super(props);
        this.onChangeDescription = this.onChangeDescription.bind(this);
        this.getTask = this.getTask.bind(this);
        this.updateCompleted = this.updateCompleted.bind(this);
        this.updateTask = this.updateTask.bind(this);
        this.deleteTask = this.deleteTask.bind(this);

        this.state = {
            currentTask: {
                id: -1,
                description: "",
                completed: false
            },
            message: ""
        };
    }

    componentDidMount() {
        this.getTask(this.props.match.params.id);
    }

    onChangeDescription(e: React.ChangeEvent<HTMLInputElement>) {
        const description = e.target.value;
        this.setState(prevState => ({
            currentTask: {
                ...prevState.currentTask,
                description: description
            }
        }));
    }

    async getTask(id: number) {
        try {
            const response = await TaskService.get(id)
            this.setState({
                currentTask: response.data
            })
        } catch (e) {
            console.log(e);
        }
    }

    async updateCompleted(status: boolean) {
        const taskDTO = {
            description: this.state.currentTask.description,
            completed: status
        };

        try {
            await TaskService.update(this.state.currentTask.id, taskDTO)
            this.setState(prevState => ({
                currentTask: {
                    ...prevState.currentTask,
                    completed: status
                }
            }));
        } catch (e) {
            console.log(e);
        }
    }

    async updateTask() {
        try {
            await TaskService.update(
                this.state.currentTask.id,
                this.state.currentTask
            );
            this.setState({
                message: "The task was updated successfully!"
            });
        } catch (e) {
            console.log(e);
        }
    }

    async deleteTask() {
        try {
            await TaskService.delete(this.state.currentTask.id);
            this.props.history.push('/task')
        } catch (e) {
            console.log(e);
        }
    }

    render() {
        const {currentTask} = this.state;

        return (
            <div>
                {currentTask ? (
                    <div className="edit-form">
                        <h4>Tutorial</h4>
                        <form>
                            <div className="form-group">
                                <label htmlFor="description">Description</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    id="description"
                                    value={currentTask.description}
                                    onChange={this.onChangeDescription}
                                />
                            </div>

                            <div className="form-group">
                                <label>
                                    <strong>Status:</strong>
                                </label>
                                {currentTask.completed ? "Completed" : "Open"}
                            </div>
                        </form>

                        {currentTask.completed ? (
                            <button
                                className="badge badge-primary mr-2"
                                onClick={() => this.updateCompleted(false)}
                            >
                                Incomplete
                            </button>
                        ) : (
                            <button
                                className="badge badge-primary mr-2"
                                onClick={() => this.updateCompleted(true)}
                            >
                                Complete
                            </button>
                        )}

                        <button
                            className="badge badge-danger mr-2"
                            onClick={this.deleteTask}
                        >
                            Delete
                        </button>

                        <button
                            type="submit"
                            className="badge badge-success"
                            onClick={this.updateTask}
                        >
                            Update
                        </button>
                        <p>{this.state.message}</p>
                    </div>
                ) : (
                    <div>
                        <br/>
                        <p>Please click on a Task...</p>
                    </div>
                )}
            </div>
        );
    }
}
