import React, {Component} from "react";
import TaskService from "../services/task.service";
import {TaskDTO} from "../models/task-dto.model";

interface State {
    id: number | null;
    description: string;
    completed: boolean;
    submitted: boolean;
}

export default class AddTask extends Component<any, State> {
    constructor(props: any) {
        super(props);
        this.onChangeDescription = this.onChangeDescription.bind(this);
        this.saveTask = this.saveTask.bind(this);
        this.newTask = this.newTask.bind(this);

        this.state = {
            id: null,
            description: "",
            completed: false,
            submitted: false
        };
    }

    onChangeDescription(e: React.ChangeEvent<HTMLInputElement>) {
        this.setState({
            description: e.target.value
        });
    }

    async saveTask() {
        const taskDTO = new TaskDTO(
            this.state.description,
            this.state.completed
        );

        try {
            const response = await TaskService.create(taskDTO)
            this.setState({
                id: response.data.id,
                description: response.data.description,
                completed: response.data.completed,
                submitted: true
            })
        } catch (e) {
            console.log(e)
        }
    }

    newTask() {
        this.setState({
            id: null,
            description: "",
            completed: false,
            submitted: false
        });
    }

    render() {
        return (
            <div className="submit-form">
                {this.state.submitted ? (
                    <div>
                        <h4>You submitted successfully!</h4>
                        <button className="btn btn-outline-success btn-sm" onClick={this.newTask}>
                            Add
                        </button>
                    </div>
                ) : (
                    <div>
                        <div className="form-group mb-2">
                            <label htmlFor="description">Description</label>
                            <input
                                type="text"
                                className="form-control"
                                id="description"
                                required
                                value={this.state.description}
                                onChange={this.onChangeDescription}
                                name="description"
                            />
                        </div>

                        <button onClick={this.saveTask} className="btn btn-outline-success btn-sm">
                            Submit
                        </button>
                    </div>
                )}
            </div>
        );
    }
}
