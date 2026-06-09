// ==========================================
// SEGURIDAD INICIAL
// ==========================================
const idDinamico = localStorage.getItem("brazoActivo");
const tokenFirebase = localStorage.getItem("firebaseToken");

if (!idDinamico || !tokenFirebase) {
    alert("No hay conexión activa. Regresando a seguridad...");
    window.location.replace("inicioSesion.html");
}

// ==========================================
// CONEXION WEBSOCKET
// ==========================================
const protocoloWS = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
const host = window.location.host;
const wsUrl = `${protocoloWS}${host}/ws/brazo?token=${tokenFirebase}`;

const socket = new WebSocket(wsUrl);
const badgeConexion = document.getElementById('badgeConexion');

socket.onopen = () => {
    console.log("Conectado al servidor de alta velocidad.");
    badgeConexion.className = "badge bg-success fs-6 py-2 px-3 rounded-pill shadow-sm";
    badgeConexion.innerText = "En Línea";

    socket.send(JSON.stringify({ //esto es para varificar de donde viene la señal de movimiento si de la web o de del ur
        tipo: "INIT",
        id_brazo: parseInt(idDinamico),
        angulos: { base:0, shoulder:0, elbow:0, wrist1:0, wrist2:0, wrist3:0 } // Mandamos ceros para que Java no marque error
    }));

};

socket.onclose = () => {
    badgeConexion.className = "badge bg-danger fs-6 py-2 px-3 rounded-pill shadow-sm";
    badgeConexion.innerText = "Desconectado";
};

socket.onmessage = (evento) => {
    const payload = JSON.parse(evento.data);

    //PARA LA CONCURRENCIA
    if(payload.tipo == "ERROR" & payload.mensaje == "OCUPADO"){
        alert("ACCESO DENEGADO: Ya esta siendo controlado y monitoreado por otro Operador")
        localStorage.removeItem("brazoActivo");
        window.location.replace("panel.html");
    }

    if (payload.id_brazo === parseInt(idDinamico)) {
        if (payload.angulos) {
            const a = payload.angulos;
            const ejes = ['Base', 'Shoulder', 'Elbow', 'Wrist1', 'Wrist2', 'Wrist3'];

            ejes.forEach(eje => {
                const key = eje.toLowerCase();
                const val = a[key] || 0;
                document.getElementById(`rango${eje}`).value = val;
                document.getElementById(`val${eje}`).innerText = `${parseFloat(val).toFixed(2)}°`;
            });

            if (window.RobotVisor) {
                window.RobotVisor.moverBase(a.base);
                window.RobotVisor.moverShoulder(a.shoulder);
                window.RobotVisor.moverElbow(a.elbow);
                window.RobotVisor.moverWrist1(a.wrist1);
                window.RobotVisor.moverWrist2(a.wrist2);
                window.RobotVisor.moverWrist3(a.wrist3);
            }
        }
    }
};

// ==========================================
//TRANSMISOR Y CONTROLES
// ==========================================
function enviarMovimiento() {
    if (socket.readyState === WebSocket.OPEN) {
        const payload = {
            tipo: "MANUAL",
            id_brazo: parseInt(idDinamico),
            angulos: {
                base:     parseFloat(document.getElementById('rangoBase').value),
                shoulder: parseFloat(document.getElementById('rangoShoulder').value),
                elbow:    parseFloat(document.getElementById('rangoElbow').value),
                wrist1:   parseFloat(document.getElementById('rangoWrist1').value),
                wrist2:   parseFloat(document.getElementById('rangoWrist2').value),
                wrist3:   parseFloat(document.getElementById('rangoWrist3').value)
            }
        };
        socket.send(JSON.stringify(payload));
    }
}

const controles = ['Base', 'Shoulder', 'Elbow', 'Wrist1', 'Wrist2', 'Wrist3'];

controles.forEach(eje => {
    const slider = document.getElementById(`rango${eje}`);
    const texto  = document.getElementById(`val${eje}`);

    slider.addEventListener('input', (event) => {
        const valor = parseFloat(event.target.value).toFixed(2);
        texto.innerText = `${valor}°`;

        if (window.RobotVisor && window.RobotVisor[`mover${eje}`]) {
            window.RobotVisor[`mover${eje}`](valor);
        }

        enviarMovimiento();
    });
});

// ==========================================
//ARRANQUE INICIAL Y CIERRE
// ==========================================

function aplicarAngulos(angulos) {
    const ejes = ['Base', 'Shoulder', 'Elbow', 'Wrist1', 'Wrist2', 'Wrist3'];

    ejes.forEach(eje => {
        const key = eje.toLowerCase();
        const val = angulos[key] ?? 0;
        document.getElementById(`rango${eje}`).value = val;
        document.getElementById(`val${eje}`).innerText = `${parseFloat(val).toFixed(2)}°`;
    });

    if (window.RobotVisor) {
        window.RobotVisor.moverBase(angulos.base);
        window.RobotVisor.moverShoulder(angulos.shoulder);
        window.RobotVisor.moverElbow(angulos.elbow);
        window.RobotVisor.moverWrist1(angulos.wrist1);
        window.RobotVisor.moverWrist2(angulos.wrist2);
        window.RobotVisor.moverWrist3(angulos.wrist3); 
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (typeof inicializarEntorno3D === 'function') {
        inicializarEntorno3D('contenedor3D');
    }

    const baseUrl = "https://brobotgcpr-production.up.railway.app/";
    fetch(`${baseUrl}/api/brazo/estado/${idDinamico}`)
        .then(res => res.json())
        .then(datos => {
            if (datos.status === "success" && datos.angulos) {
                aplicarAngulos(datos.angulos);
            }
        })
        .catch(err => console.log("Estado inicial no disponible aún:", err));
});

document.getElementById('btnCerrarSesion').addEventListener('click', async () => {
    
    localStorage.removeItem("brazoActivo");
    localStorage.removeItem("firebaseToken");
    window.location.replace("inicioSesion.html");
});