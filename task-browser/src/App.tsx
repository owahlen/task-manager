import React, {Component} from 'react';
import {BrowserRouter as Router, Switch, Route, Link} from 'react-router-dom';
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
                    <nav className="navbar navbar-expand navbar-dark bg-dark">
                        <a href="/task" className="navbar-brand">Task Manager</a>
                        <div className="navbar-nav mr-auto">
                            <li className="nav-item">
                                <Link to={"/task"} className="nav-link">Tasks</Link>
                            </li>
                            <li className="nav-item">
                                <Link to={"/add"} className="nav-link">Add</Link>
                            </li>
                        </div>
                    </nav>

                    <div className="container mt-3">
                        <Switch>
                            <Route exact path={["/", "/task"]} component={TaskList}/>
                            <Route exact path="/add" component={AddTask}/>
                            <Route path="/task/:id" component={TaskItem}/>
                        </Switch>
                    </div>

                </div>
            </Router>
        );
    }
}

export default App;
