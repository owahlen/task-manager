import {useEffect, useState} from "react";
import {useDispatch, useSelector} from "react-redux";
import {useParams} from "react-router";
import {Link} from "react-router-dom";
import {allTasks, Task} from '../modules/tasks';

const TaskDetails = () => {

    const {taskId} = useParams();
    const dispatch = useDispatch();
    const {tasks} = useSelector<{ tasks: Task[] }, { tasks: Task[] }>((state) => state);
    const [task, setTask] = useState<Task>();

    useEffect(() => {
        dispatch(allTasks())
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    useEffect(() => {
        setTask(tasks.find(task => task.id === parseInt(taskId!, 10)))
    }, [taskId, tasks]);

    return task ? (
        <div className="row">
            <div className="col-sm-12">
                <h1>Details for Task ID {task.id}</h1>
                <hr/>
                <h3>Description</h3>
                <p className="lead">{task.description}</p>
                <h3>Status</h3>
                <p className="lead">{task.status}</p>
                <hr/>
                <p>
                    <Link to="/">&laquo; back to list</Link>
                </p>
            </div>
        </div>
    ) : null
}

export default TaskDetails
