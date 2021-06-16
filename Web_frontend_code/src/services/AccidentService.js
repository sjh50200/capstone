import axios from 'axios';

const URL = 'http://localhost:8080/react/accidents';

class AccidentService {

    getAccidents(){
        return axios.get(URL);
    }
}

export default new AccidentService();