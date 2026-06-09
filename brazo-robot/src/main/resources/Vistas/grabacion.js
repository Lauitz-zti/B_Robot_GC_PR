// --- grabadora.js ---
let secuenciaGrabada = [];

function inicializarGrabadora() {
    const idDinamico = localStorage.getItem("brazoActivo");
    if (!idDinamico) return;

    const btnCapturar = document.getElementById('btnCapturarFrame');
    const btnGuardar = document.getElementById('btnGuardarRutina');
    const btnPlay = document.getElementById('btnPlayRutina');
    const contador = document.getElementById('contadorFrames');
    const selectRutinas = document.getElementById('selectRutinas');
    const baseUrl = "https://brobotgcpr-production.up.railway.app";

    //CAPTURAR UN PASO (FRAME)
    btnCapturar.addEventListener('click', () => {
        const estadoActual = {
            base: parseFloat(document.getElementById('rangoBase').value),
            shoulder: parseFloat(document.getElementById('rangoShoulder').value),
            elbow: parseFloat(document.getElementById('rangoElbow').value),
            wrist1: parseFloat(document.getElementById('rangoWrist1').value),
            wrist2: parseFloat(document.getElementById('rangoWrist2').value),
            wrist3: parseFloat(document.getElementById('rangoWrist3').value)
        };
        secuenciaGrabada.push(estadoActual);
        contador.innerText = `${secuenciaGrabada.length} Pasos`;
        
        btnCapturar.classList.replace('btn-outline-warning', 'btn-warning');
        setTimeout(() => btnCapturar.classList.replace('btn-warning', 'btn-outline-warning'), 200);
    });

    //GUARDAR LA SECUENCIA EN LA BD
    btnGuardar.addEventListener('click', () => {
        const nombre = document.getElementById('txtNombreRutina').value;
        if (secuenciaGrabada.length === 0) return alert("No has capturado ningun paso.");
        if (!nombre) return alert("Ponle un nombre a tu rutina.");

        const payload = {
            id_brazo: parseInt(idDinamico),
            nombre_estudio: nombre,
            secuencia: secuenciaGrabada
        };

        fetch(`${baseUrl}/api/recorridos/guardar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => res.json())
        .then(datos => {
            if (datos.status === "success") {
                alert("Rutina guardada exitosamente.");
                secuenciaGrabada = []; // Limpiamos la memoria
                contador.innerText = "0 Pasos";
                document.getElementById('txtNombreRutina').value = "";
                cargarRutinas(); // Actualizamos la lista
            }
        });
    });

    //CARGAR LISTA DE RUTINAS DESDE LA BD
    function cargarRutinas() {
        // Obligamos al navegador a no usar cacha para traer siempre los datos ma recientes
        fetch(`${baseUrl}/api/recorridos/listar/${idDinamico}`, 
            { 
                cache: 'no-store' 
            })
        .then(res => {
            if (!res.ok) throw new Error("Error HTTP del servidor: " + res.status);
            return res.json();
        })
        .then(rutinas => {
            selectRutinas.innerHTML = '<option value="">Selecciona una rutina...</option>';
            
            // Validamos que el backend haya enviado una lista (arreglo) y no un mensaje de error
            if (Array.isArray(rutinas)) {
                if(rutinas.length === 0) {
                    selectRutinas.innerHTML = '<option value="">(Aún no hay rutinas guardadas)</option>';
                } else {
                    rutinas.forEach(r => {
                        const option = document.createElement('option');
                        option.value = r.id_recorrido;
                        option.text = r.nombre_estudio;
                        selectRutinas.appendChild(option);
                    });
                }
            } else {
                console.error("El servidor respondio con un error o formato invalido:", rutinas);
                selectRutinas.innerHTML = '<option value="">Error interno del servidor</option>';
            }
        })
        .catch(err => {
            console.error("Fallo de red al solicitar rutinas:", err);
            selectRutinas.innerHTML = '<option value="">Fallo de conexión</option>';
        });
    }

    //REPRODUCIR LA ANIMACION
    btnPlay.addEventListener('click', () => {
        const idRecorrido = selectRutinas.value;
        if (!idRecorrido) return alert("Selecciona una rutina primero.");

        btnPlay.disabled = true;
        btnPlay.innerText = "PREPARANDO...";

        fetch(`${baseUrl}/api/recorridos/reproducir/${idRecorrido}`)
        .then(res => res.json())
        .then(secuencia => {
            
            // Verificamos si el hardware real esta conectado
            fetch(`${baseUrl}/api/puente/estado/${idDinamico}`)
            .then(res => res.json())
            .then(estadoPuente => {
                
                if (estadoPuente.conectado === true && socket.readyState === WebSocket.OPEN) {
                    // MODO INDUSTRIAL: Enviamos la macro al robot real
                    btnPlay.innerText = " BRAZO EN MOVIMIENTO";
                    const payloadMacro = {
                        tipo: "MACRO",
                        id_brazo: parseInt(idDinamico),
                        secuencia: secuencia
                    };
                    socket.send(JSON.stringify(payloadMacro));
                    
                    setTimeout(() => {
                        btnPlay.disabled = false;
                        btnPlay.innerText = "PLAY";
                    }, 2000);

                } else {
                    btnPlay.innerText = "SIMULANDO...";
                    const controles = ['Base', 'Shoulder', 'Elbow', 'Wrist1', 'Wrist2', 'Wrist3'];

                    // Funcion matematica para deslizar los sliders suavemente
                    function transicionSuave(pasoInicio, pasoFin, duracionMs) {
                        return new Promise(resolve => {
                            const tiempoInicio = performance.now();

                            function actualizar(tiempoActual) {
                                let progreso = (tiempoActual - tiempoInicio) / duracionMs;
                                if (progreso > 1) progreso = 1;

                                controles.forEach(eje => {
                                    const valInicio = pasoInicio[eje.toLowerCase()] || 0;
                                    const valFin = pasoFin[eje.toLowerCase()] || 0;
                                    
                                    const valActual = valInicio + (valFin - valInicio) * progreso;
                                    
                                    const slider = document.getElementById(`rango${eje}`);
                                    slider.value = valActual;
                                    slider.dispatchEvent(new Event('input')); // Mueve el 3D
                                });

                                if (progreso < 1) {
                                    requestAnimationFrame(actualizar);
                                } else {
                                    resolve();
                                }
                            }
                            requestAnimationFrame(actualizar);
                        });
                    }

                    // Reproductor asincrono
                    async function reproducirSecuencia() {
                        if (secuencia.length === 1) {
                            // Si solo grabaste un punto, va directo
                            controles.forEach(eje => {
                                const slider = document.getElementById(`rango${eje}`);
                                slider.value = secuencia[0][eje.toLowerCase()];
                                slider.dispatchEvent(new Event('input'));
                            });
                        } else {
                            // Si hay varios puntos, viaja suavemente entre ellos (1000ms por paso)
                            for (let i = 0; i < secuencia.length - 1; i++) {
                                await transicionSuave(secuencia[i], secuencia[i+1], 1000); 
                            }
                        }
                        
                        btnPlay.disabled = false;
                        btnPlay.innerText = "PLAY";
                    }

                    reproducirSecuencia();
                }
            })
            .catch(err => {
                alert("Error de conexión.");
                btnPlay.disabled = false;
                btnPlay.innerText = "PLAY";
            });
        });
    });

    // Cargar las rutinas al iniciar
    cargarRutinas();
}

document.addEventListener('DOMContentLoaded', inicializarGrabadora);