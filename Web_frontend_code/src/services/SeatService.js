import axios from 'axios';

const URL = 'http://localhost:8080/react/seats';

class SeatService {

    getSeats(){
        return axios.get(URL);
    }
}

export default new SeatService();