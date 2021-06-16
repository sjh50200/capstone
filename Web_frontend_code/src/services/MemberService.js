import axios from 'axios';

const URL = 'http://localhost:8080/react/members';

class MemberService {

    getMembers(){
        return axios.get(URL);
    }
}

export default new MemberService();