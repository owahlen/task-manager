import {useEffect} from "react";
import {useDispatch, useSelector} from "react-redux";
import {Link} from "react-router-dom";
import {allTasks, deleteTask, Task} from "../modules/tasks";

const TaskList = () => {

    const dispatch = useDispatch();
    const {tasks} = useSelector<{ tasks: Task[] }, { tasks: Task[] }>((state) => state);

    useEffect(() => {
        dispatch(allTasks())
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    return (
        <div className="row">
            <div className="col-sm-12">
                <h1>Tasks to Read Before You Die</h1>
                <table className="table table-striped">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Description</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    {tasks.map((task) => (
                        <tr key={task.id}>
                            <td>{task.id}</td>
                            <td>
                                <Link to={`/tasks/${task.id}`}>{task.description}</Link>
                            </td>
                            <td>{task.status}</td>
                            <td>
                                <button className="btn btn-xs btn-danger" onClick={() => dispatch(deleteTask(task))}>
                                    Delete Task
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default TaskList
