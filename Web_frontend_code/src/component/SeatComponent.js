import React from 'react';
import SeatService from '../services/SeatService';

class SeatComponent extends React.Component {

    constructor(props) {
        super(props)
        this.state = {
            seats:[]
        }
    }

    componentDidMount() {
        SeatService.getSeats().then((response) => {
            this.setState({ seats: response.data})
        });
    }

    render() {
        return (
            <div className = "tableDiv">
                <h4 className = "text-Center"> Seats List</h4>
                <br></br>
                <table className = "table table-bordered">
                    <thead>
                        <tr className = "table-success">
                            <td> seat_num</td>
                            <td> status</td>
                            <td> car_num</td>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            this.state.seats.map(
                                seat =>
                                <tr key = {seat.seatNum}>
                                    <td> {seat.seatNum}</td>
                                    <td> {seat.status}</td>
                                    <td> {seat.carNum}</td>
                                </tr>
                            )
                        }
                    </tbody>
                </table>
            </div>
        )
    }
}

export default SeatComponent;