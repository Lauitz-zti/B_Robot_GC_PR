// Renderizado 3D del robot usando Three.js - Estilo UR3e

window.RobotVisor = {
    moverBase: function(val) {},
    moverShoulder: function(val) {}, 
    moverElbow: function(val) {},    
    moverWrist1: function(val) {},   
    moverWrist2: function(val) {},   
    moverWrist3: function(val) {}    
};

function inicializarEntorno3D(idContenedor) {
    const contenedor = document.getElementById(idContenedor);
    const escena = new THREE.Scene();
    
    // Perspectiva de camara ajustada
    const camara = new THREE.PerspectiveCamera(45, contenedor.clientWidth / contenedor.clientHeight, 0.1, 100);
    camara.position.set(4, 5, 7);
    camara.lookAt(0, 1.5, 0);

    const renderizador = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    renderizador.setSize(contenedor.clientWidth, contenedor.clientHeight);
    contenedor.appendChild(renderizador.domElement);

    //ILUMINACION 
    const luzAmbiente = new THREE.AmbientLight(0xffffff, 0.6);
    escena.add(luzAmbiente);
    const luzDireccional = new THREE.DirectionalLight(0xffffff, 0.8);
    luzDireccional.position.set(5, 10, 5);
    escena.add(luzDireccional);
    const luzResplandor = new THREE.PointLight(0xffffff, 0.5);
    luzResplandor.position.set(-5, 5, -5);
    escena.add(luzResplandor);

    const grid = new THREE.GridHelper(10, 10, 0x444444, 0x222222);
    escena.add(grid);

    // MATERIALES ESTILO UNIVERSAL ROBOTS
    const matURMetal = new THREE.MeshStandardMaterial({ color: 0xcccccc, roughness: 0.3, metalness: 0.7 }); // Aluminio
    const matURBlue = new THREE.MeshStandardMaterial({ color: 0x6ca0dc, roughness: 0.4, metalness: 0.2 }); // Tapas azules
    const matURDark = new THREE.MeshStandardMaterial({ color: 0x333333, roughness: 0.6, metalness: 0.4 }); // Juntas negras

    // FABRICACIÓN DE PIEZAS (Cilindricas)
    
    // Simula los motores redondos caracteristicos del UR
    function crearArticulacionUR(radio, altura, colorTapa) {
        const grupo = new THREE.Group();
        // Cuerpo del motor (Gris oscuro)
        const motor = new THREE.Mesh(new THREE.CylinderGeometry(radio, radio, altura, 32), matURDark);
        motor.rotation.x = Math.PI / 2;
        grupo.add(motor);
        // Tapa caracteristica (Azul o Metal)
        const tapa1 = new THREE.Mesh(new THREE.CylinderGeometry(radio * 1.05, radio * 1.05, altura * 0.1, 32), colorTapa);
        tapa1.rotation.x = Math.PI / 2;
        tapa1.position.z = altura / 2;
        grupo.add(tapa1);
        const tapa2 = new THREE.Mesh(new THREE.CylinderGeometry(radio * 1.05, radio * 1.05, altura * 0.1, 32), colorTapa);
        tapa2.rotation.x = Math.PI / 2;
        tapa2.position.z = -altura / 2;
        grupo.add(tapa2);
        
        return grupo;
    }

    // Simula los eslabones tubulares de aluminio
    function crearTuboUR(longitud, radio) {
        const grupo = new THREE.Group();
        const tubo = new THREE.Mesh(new THREE.CylinderGeometry(radio, radio, longitud, 32), matURMetal);
        tubo.position.y = longitud / 2; // El pivote queda en la base del tubo
        grupo.add(tubo);
        return grupo;
    }

    // Pinza simple industrial
    function crearPinza() {
        const grupo = new THREE.Group();
        const base = new THREE.Mesh(new THREE.CylinderGeometry(0.15, 0.15, 0.1, 32), matURDark);
        grupo.add(base);
        const dedoIzq = new THREE.Mesh(new THREE.BoxGeometry(0.05, 0.3, 0.1), matURMetal);
        dedoIzq.position.set(-0.1, 0.15, 0);
        grupo.add(dedoIzq);
        const dedoDer = new THREE.Mesh(new THREE.BoxGeometry(0.05, 0.3, 0.1), matURMetal);
        dedoDer.position.set(0.1, 0.15, 0);
        grupo.add(dedoDer);
        return grupo;
    }

    /* ENSAMBLAJE DEL ROBOT UR3e */
    const robotRaiz = new THREE.Group();
    escena.add(robotRaiz);

    // 1. BASE
    const grupoBase = new THREE.Group();
    robotRaiz.add(grupoBase);
    
    const baseCilindro = new THREE.Mesh(new THREE.CylinderGeometry(0.3, 0.35, 0.2, 32), matURMetal);
    baseCilindro.position.y = 0.1;
    grupoBase.add(baseCilindro);
    
    const motorBase = crearArticulacionUR(0.2, 0.3, matURBlue);
    motorBase.position.y = 0.35;
    motorBase.rotation.y = Math.PI / 2;
    motorBase.rotation.z = Math.PI / 2;
    grupoBase.add(motorBase);

    // 2. HOMBRO (Shoulder)
    const grupoHombro = new THREE.Group();
    grupoHombro.position.y = 0.35; 
    grupoBase.add(grupoHombro); 
    
    const motorHombro = crearArticulacionUR(0.2, 0.3, matURBlue);
    grupoHombro.add(motorHombro);

    const brazoLargo = crearTuboUR(1.2, 0.15); // Eslabon superior
    grupoHombro.add(brazoLargo);

    // 3. CODO (Elbow)
    const grupoCodo = new THREE.Group();
    grupoCodo.position.y = 1.2; // Al final del brazo largo
    grupoHombro.add(grupoCodo);

    const motorCodo = crearArticulacionUR(0.18, 0.25, matURBlue);
    grupoCodo.add(motorCodo);

    const antebrazo = crearTuboUR(1.0, 0.12); // Eslaboon inferior
    grupoCodo.add(antebrazo);

    // 4. MUÑECA 1 (Wrist 1)
    const grupoWrist1 = new THREE.Group();
    grupoWrist1.position.y = 1.0; 
    grupoCodo.add(grupoWrist1);

    const motorWrist1 = crearArticulacionUR(0.15, 0.2, matURMetal);
    grupoWrist1.add(motorWrist1);

    // 5. MUÑECA 2 (Wrist 2)
    const grupoWrist2 = new THREE.Group();
    grupoWrist2.position.z = 0.2; 
    grupoWrist1.add(grupoWrist2);

    const motorWrist2 = crearArticulacionUR(0.15, 0.2, matURMetal);
    motorWrist2.rotation.y = Math.PI / 2;
    grupoWrist2.add(motorWrist2);

    // 6. MUÑECA 3 (Wrist 3)
    const grupoWrist3 = new THREE.Group();
    grupoWrist3.position.y = 0.2; 
    grupoWrist2.add(grupoWrist3);

    const motorWrist3 = crearArticulacionUR(0.12, 0.15, matURMetal);
    motorWrist3.rotation.x = Math.PI / 2;
    grupoWrist3.add(motorWrist3);

    const pinza = crearPinza();
    pinza.position.y = 0.1;
    grupoWrist3.add(pinza);

    // ENLACE CON LOS SLIDERS
    window.RobotVisor.moverBase = (val) => grupoBase.rotation.y = THREE.MathUtils.degToRad(val);
    window.RobotVisor.moverShoulder = (val) => grupoHombro.rotation.x = THREE.MathUtils.degToRad(val);
    window.RobotVisor.moverElbow = (val) => grupoCodo.rotation.x = THREE.MathUtils.degToRad(val);
    window.RobotVisor.moverWrist1 = (val) => grupoWrist1.rotation.x = THREE.MathUtils.degToRad(val);
    window.RobotVisor.moverWrist2 = (val) => grupoWrist2.rotation.z = THREE.MathUtils.degToRad(val);
    window.RobotVisor.moverWrist3 = (val) => grupoWrist3.rotation.y = THREE.MathUtils.degToRad(val);

    // BUCLE DE ANIMACION
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