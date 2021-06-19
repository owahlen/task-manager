import React, {Component} from "react";
import TaskService from "../services/task.service";
import {Link} from "react-router-dom";
import {Task} from "../models/task.model";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faSearch} from "@fortawesome/free-solid-svg-icons";

interface State {
    tasks: Array<Task>;
    currentTask: Task | null;
    currentIndex: number;
    searchDescription: string;
}

export default class TaskList extends Component<any, State> {

    constructor(props: any) {
        super(props);
        this.onChangeSearchDescription = this.onChangeSearchDescription.bind(this);
        this.retrieveTasks = this.retrieveTasks.bind(this);
        this.refreshList = this.refreshList.bind(this);
        this.setActiveTask = this.setActiveTask.bind(this);
        this.searchDescription = this.searchDescription.bind(this);

        this.state = {
            tasks: Array<Task>(),
            currentTask: null,
            currentIndex: -1,
            searchDescription: ""
        };
    }

    componentDidMount() {
        this.retrieveTasks();
    }

    onChangeSearchDescription(e: React.ChangeEvent<HTMLInputElement>) {
        const searchDescription = e.target.value;

        this.setState({
            searchDescription: searchDescription
        });
    }

    async retrieveTasks() {
        try {
            const response = await TaskService.getAll();
            this.setState({tasks: response.data})
        } catch (e) {
            console.log(e)
        }
    }

    refreshList() {
        this.retrieveTasks();
        this.setState({
            currentTask: null,
            currentIndex: -1
        });
    }

    setActiveTask(task: Task, index: number) {
        this.setState({
            currentTask: task,
            currentIndex: index
        });
    }

    async searchDescription() {
        try {
            const response = await TaskService.findByDescription(this.state.searchDescription);
            this.setState({
                tasks: response.data
            })
        } catch (e) {
            console.log(e)
        }
    }

    render() {
        const {searchDescription, tasks, currentTask, currentIndex} = this.state;

        return (
            <div className="list row">
                <div className="col-md-8">
                    <div className="input-group mt-4 mb-4">
                        <div className="form-outline">
                            <input
                                type="search"
                                className="form-control"
                                placeholder="Search"
                                value={searchDescription}
                                onChange={this.onChangeSearchDescription}/>
                        </div>
                        <button
                            className="btn btn-primary"
                            type="button"
                            onClick={this.searchDescription}>
                            <FontAwesomeIcon icon={faSearch} />
                        </button>
                    </div>
                </div>
                <div className="col-md-6">
                    <h4>Tasks List</h4>

                    <ul className="list-group">
                        {tasks &&
                        tasks.map((task, index: number) => (
                            <li
                                className={
                                    "list-group-item " +
                                    (index === currentIndex ? "active" : "")
                                }
                                onClick={() => this.setActiveTask(task, index)}
                                key={index}>
                                {task.description}
                            </li>
                        ))}
                    </ul>

                </div>
                <div className="col-md-6">
                    {currentTask ? (
                        <div>
                            <h4>Task</h4>
                            <div>
                                <label>
                                    <strong>Description:</strong>
                                </label>{" "}
                                {currentTask.description}
                            </div>
                            <div>
                                <label>
                                    <strong>Status:</strong>
                                </label>{" "}
                                {currentTask.completed ? "Completed" : "Open"}
                            </div>

                            <Link to={"/task/" + currentTask.id} className="btn btn-outline-warning btn-sm">
                                Edit
                            </Link>
                        </div>
                    ) : (
                        <div>
                            <br/>
                            <p>Please click on a Task...</p>
                        </div>
                    )}
                </div>
            </div>
        );
    }
}
