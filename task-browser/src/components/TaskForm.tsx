import {FormEvent, useState} from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router";
import { addTask } from "../modules/tasks";
import PrivateRoute from "./RenderOnRole";
import {AxiosResponse} from "axios";

const TaskForm = () => {

    const [description, setDescription] = useState('');
    const [status, setStatus] = useState('');

    const dispatch = useDispatch();
    const navigate = useNavigate();

    const handleSubmit = (event: FormEvent) => {
        event.preventDefault();
        if (!description || !status) {
            return;
        }
        const axiosResponse = dispatch(addTask({description, status})) as unknown as Promise<AxiosResponse>
        axiosResponse.then(() => navigate("/"))
    };

    return (
        <div className="row">
            <div className="col-sm-6">
                <form onSubmit={handleSubmit}>
                    <h1>Add a new task:</h1>
                    <div className="form-group">
                        <label htmlFor="description">Description</label>
                        <input type="text" className="form-control" placeholder="Description"
                               value={description} onChange={(e) => setDescription(e.target.value)}/>
                    </div>
                    <div className="form-group">
                        <label htmlFor="status">Status</label>
                        <input type="text" className="form-control" placeholder="Status"
                               value={status} onChange={(e) => setStatus(e.target.value)}/>
                    </div>
                    <PrivateRoute roles={['ROLE_USER']}>
                        <button type="submit" className="btn btn-primary">Add task</button>
                    </PrivateRoute>
                </form>
            </div>
        </div>
    );
}

export default TaskForm
