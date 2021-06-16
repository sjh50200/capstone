import React from 'react';
import MemberService from '../services/MemberService';

class MemberComponent extends React.Component {

    constructor(props) {
        super(props)
        this.state = {
            members:[]
        }
    }

    componentDidMount() {
        MemberService.getMembers().then((response) => {
            this.setState({ members: response.data})
        });
    }

    render() {
        return (
            <div className = "tableDiv">
                <h4 className = "text-Center"> Members List</h4>
                <br></br>
                <table className = "table table-bordered">
                    <thead>
                        <tr className = "table-success">
                            <td> member_num</td>
                            <td> id</td>
                            <td> car_num</td>
                            <td> entry_time</td>
                            <td> is_parked</td>
                            <td> seat_num</td>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            this.state.members.map(
                                member =>
                                <tr key = {member.number}>
                                    <td> {member.number}</td>
                                    <td> {member.id}</td>
                                    <td> {member.carNum}</td>
                                    <td> {member.entryTime}</td>
                                    <td> {member.isParked}</td>
                                    <td> {member.seatNum}</td>
                                </tr>
                            )
                        }
                    </tbody>
                </table>
            </div>
        )
    }
}

export default MemberComponent;