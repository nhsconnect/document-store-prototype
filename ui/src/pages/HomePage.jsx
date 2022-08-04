import React from "react";
import {Link} from "react-router-dom";

const HomePage = () => {
    return (
        <>
            <h3>Document Store</h3>
            <p>Use this service to:</p>
            <ul>
                <li><Link to="/search">View Stored Patient Record</Link></li>
                <li><Link to="/upload">Upload Patient Record</Link></li>
            </ul>
        </>
    )
}

export default HomePage