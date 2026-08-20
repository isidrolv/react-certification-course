import React, {useState, useEffect} from 'react';

export default function UserProfile({userId = 1}) {
    const [userData, setUserData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (userId == null) {
            setUserData(null);
            setLoading(false);
            return;
        }

        setLoading(true);
        fetch(`https://jsonplaceholder.typicode.com/users/${userId}`)
            .then((res) => res.json())
            .then((data) => {
                setUserData(data);
                setLoading(false);
            });
    }, [userId]); // <-- Observar aquí

    if (loading) return <p>Cargando perfil...</p>;

    return (
        <div style={{border: '1px solid #ccc', padding: '15px', borderRadius: '8px'}}>
            <h3>Nombre: {userData?.name}</h3>
            <p>Email: {userData?.email}</p>
            <p>Compañía: {userData?.company?.name}</p>
        </div>
    );
}
