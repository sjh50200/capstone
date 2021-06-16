import axios from 'axios';

const URL = 'http://localhost:8080/react/logs';

class LogService {

    getLogs(){
        return axios.get(URL);
    }
}

export default new LogService();