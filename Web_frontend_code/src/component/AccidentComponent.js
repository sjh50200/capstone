import React from 'react';
import AccidentService from '../services/AccidentService';

class AccidentComponent extends React.Component {

    constructor(props) {
        super(props)
        this.state = {
            accidents:[]
        }
    }

    componentDidMount() { //생성자 느낌 페이지가 만들어질 때 호출
        //mount시점 데이터 세팅 -> create -> webpage구현 
        AccidentService.getAccidents().then((response) => {
            this.setState({ accidents: response.data})
        });
    }

    render() {
        return (
            <div className = "tableDiv">
                <h4 className = "text-Center"> Accident List</h4>
                <br></br>
                <table className = "table table-bordered">
                    <thead>
                        <tr className = "table-success">
                            <td> acc_num</td>
                            <td> acc_time</td>
                            <td> car_num1</td>
                            <td> car_num2</td>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            this.state.accidents.map(
                                accident =>
                                <tr key = {accident.accNum}>
                                    <td> {accident.accNum}</td>
                                    <td> {accident.accTime}</td>
                                    <td> {accident.carNum1}</td>
                                    <td> {accident.carNum2}</td>
                                </tr>
                            )
                        }
                    </tbody>
                </table>
            </div>
        )
    }
}

export default AccidentComponent;