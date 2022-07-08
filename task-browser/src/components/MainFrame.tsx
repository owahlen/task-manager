import {Route, Routes} from "react-router-dom";
import TaskDetails from "./TaskDetails";
import TaskForm from "./TaskForm";
import TaskList from "./TaskList";
import Menu from "./Menu";
import NoMatch from "./NoMatch";
import SecretTasks from "./SecretTasks";
import PrivateRoute from "./PrivateRoute";

const MainFrame = () => (
    <>
        <Menu/>
        <Routes>
            <Route path="/" element={<TaskList/>}/>
            <Route path="/task/new" element={<TaskForm/>}/>
            <Route path="/task/:taskId" element={<TaskDetails/>}/>
            <Route path="/secret" element={<PrivateRoute roles={['ROLE_ADMIN']}/>}>
                <Route path="" element={<SecretTasks/>}/>
            </Route>
            <Route path="*" element={<NoMatch/>}/>
        </Routes>
    </>
)

export default MainFrame
