import './App.css';
import React, { Component } from 'react';
import { BrowserRouter, Route, Link, Switch } from "react-router-dom";
import MemberComponent from './component/MemberComponent';
import SeatComponent from './component/SeatComponent';
import LogComponent from './component/LogComponent';
import AccidentComponent from './component/AccidentComponent';
import Header from './pages/Header';
import './css/App.css';

function App() {
  return (
    <div className="App">
      <BrowserRouter>
       <Link to = "/"><Header /></Link>
       <br></br><br></br><br></br>
        <div className = "menuBar">
          <div className = "menu">
          <button type="button" class="btn btn-success"><Link to="/member" className="linkStyle">
            member table</Link></button>
          </div>
          <div className = "menu">
          <button type="button" class="btn btn-success"><Link to="/seat" className="linkStyle">
            seat table</Link></button>
          </div>
          <div className = "menu">
          <button type="button" class="btn btn-success"><Link to="/log" className="linkStyle">
            log table</Link></button>
          </div>
          <div className = "menu">
          <button type="button" class="btn btn-success"><Link to="/accident" className="linkStyle">
            accident table</Link></button>
          </div>
        </div>
        <br></br><br></br>
        <Switch>
          <Route path="/member" exact component={Member}></Route>
          <Route path="/seat" component={Seat}></Route>
          <Route path="/log" component={Log}></Route>
          <Route path="/accident" component={Accident}></Route>
        </Switch>
      </BrowserRouter>
    </div>
  );
}

function Member() {
  return <div><MemberComponent /></div>;
}

function Seat() {
  return <div><SeatComponent /></div>;
}

function Log() {
  return <div><LogComponent /></div>;
}

function Accident() {
  return <div><AccidentComponent /></div>;
}

export default App;
