import React from 'react';
import LogService from '../services/LogService';

class LogComponent extends React.Component {

    constructor(props) {
        super(props)
        this.state = {
            logs:[]
        }
    }

    componentDidMount() {
        LogService.getLogs().then((response) => {
            this.setState({ logs: response.data})
        });
    }

    render() {
        return (
            <div className = "tableDiv">
                <h4 className = "text-Center"> Logs</h4>
                <br></br>
                <table className = "table table-bordered">
                    <thead>
                        <tr className = "table-success">
                            <td> log_num</td>
                            <td> car_num</td>
                            <td> entry_time</td>
                            <td> exit_time</td>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            this.state.logs.map(
                                log =>
                                <tr key = {log.logNum}>
                                    <td> {log.logNum}</td>
                                    <td> {log.carNum}</td>
                                    <td> {log.entryTime}</td>
                                    <td> {log.exitTime}</td>
                                </tr>
                            )
                        }
                    </tbody>
                </table>
            </div>
        )
    }
}

export default LogComponent;