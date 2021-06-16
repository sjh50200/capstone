import React from 'react';
import logo from '../img/logo.png';

function Header({location, history}) {
    return(
    <header className = 'header'>
        <img src = {logo}></img>
    </header>
    );
}
export default Header;