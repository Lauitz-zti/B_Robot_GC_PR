function inicializarLogger() {
    const btnGuardar = document.getElementById('btnEnviarPosicion');

    if (!btnGuardar) return; 

    btnGuardar.addEventListener('click', () => {
        const idDinamico = localStorage.getItem("brazoActivo");

        if (!idDinamico) {
            alert("Sesión no válida para guardar posiciones.");
            return;
        }

        //Guarda los angulos
        const angulosActuales = {
            base: parseFloat(document.getElementById('rangoBase').value),
            shoulder: parseFloat(document.getElementById('rangoShoulder').value),
            elbow: parseFloat(document.getElementById('rangoElbow').value),
            wrist1: parseFloat(document.getElementById('rangoWrist1').value),
            wrist2: parseFloat(document.getElementById('rangoWrist2').value),
            wrist3: parseFloat(document.getElementById('rangoWrist3').value)
        };

        //Actualizamos la posicion del brazo, el catalogo manda logger INFO_POS
        const payloadLogger = {
            id_brazo: parseInt(idDinamico),
            codigo_error: "INFO_POS", // guardado para la posicion estatica
            mensaje: `Configuración de ejes: ${JSON.stringify(angulosActuales)}`
        };

        //Modifica la interfaz
        const textoOriginal = btnGuardar.innerText;
        btnGuardar.innerText = "GUARDANDO...";
        btnGuardar.disabled = true;

        //Avisamos que hicimos cambios
        const baseUrl = "https://brobotgcpr-production.up.railway.app"; //Necesario pq lo subimos a un servidor ya no estamos desde local
        fetch(`${baseUrl}/api/logger/registrar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payloadLogger)
        })
        .then(res => res.json())
        .then(datos => {
            if(datos.status === "success") {
                btnGuardar.innerText = "¡REGISTRADO!";
                btnGuardar.classList.replace("btn-primary", "btn-success");
            } else {
                alert("Aviso del servidor: " + datos.mensaje);
                btnGuardar.innerText = "FALLO AL GUARDAR";
                btnGuardar.classList.replace("btn-primary", "btn-warning");
            }
            
            //El boton vuelve a su estado original (color azul)
            setTimeout(() => {
                btnGuardar.innerText = textoOriginal;
                btnGuardar.classList.remove("btn-success", "btn-warning");
                btnGuardar.classList.add("btn-primary");
                btnGuardar.disabled = false;
            }, 2000);
        })
        .catch(err => {
            console.error("Error de conexión con la bitacora:", err);
            btnGuardar.innerText = "ERROR DE RED";
            btnGuardar.classList.replace("btn-primary", "btn-danger");
            
            setTimeout(() => {
                btnGuardar.innerText = textoOriginal;
                btnGuardar.classList.replace("btn-danger", "btn-primary");
                btnGuardar.disabled = false;
            }, 2000);
        });
    });
}

document.addEventListener('DOMContentLoaded', inicializarLogger);