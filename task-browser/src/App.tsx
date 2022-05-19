import React, {Component} from 'react';
import {BrowserRouter as Router, Link, Route, Routes} from 'react-router-dom';
import "bootstrap/dist/css/bootstrap.min.css";
import './App.css';
import TaskList from "./components/task-list.component";
import AddTask from "./components/add-task.component";
import TaskItem from "./components/task-item.component";

class App extends Component {
    render() {
        return (
            <Router>
                <div>
                    <nav className="navbar navbar-expand-lg navbar-light bg-light">
                        <div className="container-fluid">
                            <a href="/" className="navbar-brand">Task Manager</a>
                            <button className="navbar-toggler" type="button" data-bs-toggle="collapse"
                                    data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent"
                                    aria-expanded="false" aria-label="Toggle navigation">
                                <span className="navbar-toggler-icon"/>
                            </button>
                            <div className="collapse navbar-collapse" id="navbarSupportedContent">
                                <div className="navbar-nav me-auto mb-2 mb-lg-0">
                                    <li className="nav-item">
                                        <Link to={"/task"} className="nav-link active" aria-current="page">Tasks</Link>
                                    </li>
                                    <li className="nav-item">
                                        <Link to={"/add"} className="nav-link">Add</Link>
                                    </li>
                                </div>
                            </div>
                        </div>
                    </nav>

                    <div className="container mt-3">
                        <Routes>
                            <Route exact path="/" element={<TaskList/>}/>
                            <Route exact path="/task" element={<TaskList/>}/>
                            <Route exact path="/add" element={<AddTask/>}/>
                            <Route path="/task/:id" element={<TaskItem/>}/>
                        </Routes>
                    </div>

                </div>
            </Router>
        );
    }
}

export default App;
