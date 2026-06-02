//Renderizado 3D del robot usando Three.js

window.RobotVisor = {
    moverBase: function(val) {},
    moverHombro: function(val) {},
    moverCodo: function(val) {},
    moverPitch: function(val) {},
    moverRoll: function(val) {},
    moverYaw: function(val) {} // Pinza
};

function inicializarEntorno3D(idContenedor) {
    const contenedor = document.getElementById(idContenedor);
    const escena = new THREE.Scene();
    
    //Perspectiva de camara ubicada para mostrar el robot desde un angulo especifico
    const camara = new THREE.PerspectiveCamera(45, contenedor.clientWidth / contenedor.clientHeight, 0.1, 100);
    camara.position.set(3, 4, 6);
    camara.lookAt(0, 1.5, 0);

    const renderizador = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    renderizador.setSize(contenedor.clientWidth, contenedor.clientHeight);
    contenedor.appendChild(renderizador.domElement);

    //ILUMINACION 
    //Migramos lo que teniamos en RobotRenderer.java pero adaptado a 
    // Three.js, con una luz ambiental para suavizar sombras y una direccional para dar profundidad
    const luzAmbiente = new THREE.AmbientLight(0xffffff, 0.6);
    escena.add(luzAmbiente);
    const luzDireccional = new THREE.DirectionalLight(0xffffff, 0.8);
    luzDireccional.position.set(5, 10, 5);
    escena.add(luzDireccional);

    const grid = new THREE.GridHelper(10, 10, 0x444444, 0x222222);
    escena.add(grid);

    //MATERIALES   
    //Migramos los colores y propiedades de los materiales que teniamos en RobotParts.java, 
    //usando MeshStandardMaterial para un aspecto mas realista con iluminacion
    const matAzul = new THREE.MeshStandardMaterial({ color: 0x1A66E6, roughness: 0.4, metalness: 0.3 });
    const matNegro = new THREE.MeshStandardMaterial({ color: 0x222222, roughness: 0.8 });
    const matGris = new THREE.MeshStandardMaterial({ color: 0x888888, metalness: 0.8, roughness: 0.2 });

    //FABRICACION DE PIEZAS
    // Creamos funciones para generar las partes del robot, como los servos, brackets y pinza,
    // usando geometrías básicas de Three.js y aplicando los materiales definidos
    //RobotRenderer.java tenia un metodo crearServo() que generaba un servo con un cuerpo central, orejas y un eje rotatorio,
    function crearServo() {
        const grupo = new THREE.Group();
        // Cuerpo central
        const cuerpo = new THREE.Mesh(new THREE.BoxGeometry(0.4, 0.5, 0.3), matNegro);
        grupo.add(cuerpo);
        // Orejas
        const orejas = new THREE.Mesh(new THREE.BoxGeometry(0.6, 0.05, 0.3), matNegro);
        orejas.position.y = 0.15;
        grupo.add(orejas);
        // Eje rotatorio
        const eje = new THREE.Mesh(new THREE.CylinderGeometry(0.08, 0.08, 0.1, 16), matGris);
        eje.rotation.x = Math.PI / 2;
        eje.position.set(0, 0.25, 0.15);
        grupo.add(eje);
        return grupo;
    }

    function crearBracketU(w, h, d) {
        const grupo = new THREE.Group();
        const espesor = 0.05;
        // Fondo
        const fondo = new THREE.Mesh(new THREE.BoxGeometry(w, espesor, d), matAzul);
        fondo.position.y = -h / 2;
        grupo.add(fondo);
        // Pared Izquierda
        const pIzq = new THREE.Mesh(new THREE.BoxGeometry(espesor, h, d), matAzul);
        pIzq.position.x = -w / 2 + espesor / 2;
        grupo.add(pIzq);
        // Pared Derecha
        const pDer = new THREE.Mesh(new THREE.BoxGeometry(espesor, h, d), matAzul);
        pDer.position.x = w / 2 - espesor / 2;
        grupo.add(pDer);
        
        return grupo;
    }

    function crearPinza() {
        const grupo = new THREE.Group();
        // Base de pinza
        const base = new THREE.Mesh(new THREE.BoxGeometry(0.6, 0.1, 0.3), matGris);
        grupo.add(base);
        // Dedos fijos por ahora para visualización
        const dedoIzq = new THREE.Mesh(new THREE.BoxGeometry(0.1, 0.5, 0.1), matGris);
        dedoIzq.position.set(-0.25, 0.25, 0);
        grupo.add(dedoIzq);
        const dedoDer = new THREE.Mesh(new THREE.BoxGeometry(0.1, 0.5, 0.1), matGris);
        dedoDer.position.set(0.25, 0.25, 0);
        grupo.add(dedoDer);
        return grupo;
    }

    /* ENSAMBLAJE DEL ROBOT
    Creamos una jerarquía de grupos para cada articulación del robot,
    permitiendo que las transformaciones se apliquen de manera acumulativa y reflejen la estructura del robot
    */
    const robotRaiz = new THREE.Group();
    escena.add(robotRaiz);

    //BASE
    const grupoBase = new THREE.Group();
    robotRaiz.add(grupoBase);
    
    const mallaBase = new THREE.Mesh(new THREE.CylinderGeometry(0.6, 0.6, 0.2, 32), matAzul);
    mallaBase.position.y = 0.1;
    grupoBase.add(mallaBase);

    const servoBase = crearServo();
    servoBase.position.y = 0.35;
    grupoBase.add(servoBase);

    //HOMBRO
    const grupoHombro = new THREE.Group();
    grupoHombro.position.y = 0.55; 
    grupoBase.add(grupoHombro); 
    
    const servoHombro = crearServo();
    servoHombro.position.y = 0.2;
    servoHombro.rotation.z = -Math.PI / 2; 
    grupoHombro.add(servoHombro);

    // En Java el BracketU tiene alto 0.4 y se escala 2.5 (0.4 * 2.5 = 1.0)
    const brazoLargo = crearBracketU(0.45, 1.0, 0.4); 
    brazoLargo.position.y = 0.7;
    grupoHombro.add(brazoLargo);

    //CODO
    const grupoCodo = new THREE.Group();
    grupoCodo.position.y = 1.2; 
    grupoHombro.add(grupoCodo);

    const servoCodo = crearServo();
    servoCodo.rotation.z = -Math.PI / 2;
    grupoCodo.add(servoCodo);

    // En Java el antebrazo tiene alto 0.4 y se escala 1.5 (0.4 * 1.5 = 0.6)
    const antebrazo = crearBracketU(0.4, 0.6, 0.4);
    antebrazo.position.y = 0.4;
    grupoCodo.add(antebrazo);

    //MUÑECA Y PINZA
    const grupoMuneca = new THREE.Group();
    grupoMuneca.position.y = 0.75; 
    grupoCodo.add(grupoMuneca);

    const servoMuneca = crearServo();
    grupoMuneca.add(servoMuneca);

    const grupoPitch = new THREE.Group();
    grupoPitch.position.y = 0.25;
    grupoMuneca.add(grupoPitch);

    const grupoYawPinza = new THREE.Group();
    grupoYawPinza.position.y = 0.15;
    grupoPitch.add(grupoYawPinza);

    const pinza = crearPinza();
    grupoYawPinza.add(pinza);

    //
    window.RobotVisor.moverBase = (val) => grupoBase.rotation.y = THREE.MathUtils.degToRad(val);
    window.RobotVisor.moverHombro = (val) => grupoHombro.rotation.x = THREE.MathUtils.degToRad(val);
    window.RobotVisor.moverCodo = (val) => grupoCodo.rotation.x = THREE.MathUtils.degToRad(val);
    window.RobotVisor.moverPitch = (val) => grupoPitch.rotation.x = THREE.MathUtils.degToRad(val);
    window.RobotVisor.moverRoll = (val) => grupoMuneca.rotation.y = THREE.MathUtils.degToRad(val);

    // Para la interfaz usaremos Yaw temporalmente para rotar la pinza entera
    window.RobotVisor.moverYaw = (val) => grupoYawPinza.rotation.z = THREE.MathUtils.degToRad(val);

    //BUCLE DE ANIMACION
    //Usamos requestAnimationFrame para crear un bucle de renderizado eficiente, 
    // que actualiza la escena cada vez que el navegador esté listo para repintar
    // Esto asegura que las animaciones sean suaves y que el rendimiento sea óptimo, 
    // adaptándose a la capacidad de renderizado del dispositivo
    //En cada frame, renderizamos la escena desde la perspectiva de la cámara, mostrando el robot con sus transformaciones actualizadas
    function animar() {
        requestAnimationFrame(animar);
        renderizador.render(escena, camara);
    }
    animar();

    window.addEventListener('resize', () => {
        camara.aspect = contenedor.clientWidth / contenedor.clientHeight;
        camara.updateProjectionMatrix();
        renderizador.setSize(contenedor.clientWidth, contenedor.clientHeight);
    });
}