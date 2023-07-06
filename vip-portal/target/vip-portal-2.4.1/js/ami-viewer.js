// Code taken from AMI lesson 3:
// https://codesandbox.io/s/github/FNNDSC/ami/tree/master/lessons/03

function amiViewer(url, rawExtension, divId) {
  const filename = new URL(url).searchParams.get('filename');
  window.setNoticeMessage(
    'Loading and parsing image ' +
      filename +
      '.  Please wait, this may take a while …');

  const colors = {
    red: 0xff0000,
    darkGrey: 0x353535
  };

  const container = document.getElementById(divId);

  // Setup renderer
  const renderer = new window.THREE.WebGLRenderer({
    antialias: true,
  });
  renderer.setSize(container.offsetWidth, container.offsetHeight);
  renderer.setClearColor(colors.darkGrey, 1);
  renderer.setPixelRatio(window.devicePixelRatio);
  container.appendChild(renderer.domElement);

  const scene = new window.THREE.Scene();

  const camera = new window.AMI.OrthographicCamera(
    container.clientWidth / -2,
    container.clientWidth / 2,
    container.clientHeight / 2,
    container.clientHeight / -2,
    0.1,
    10000
  );

  // Setup controls
  const controls = new window.AMI.TrackballOrthoControl(camera, container);
  controls.staticMoving = true;
  controls.noRotate = true;
  camera.controls = controls;

  const onWindowResize = function() {
    camera.canvas = {
      width: container.offsetWidth,
      height: container.offsetHeight,
    };
    camera.fitBox(2);

    renderer.setSize(container.offsetWidth, container.offsetHeight);
  }
  window.addEventListener('resize', onWindowResize, false);

  const loader = new window.AMI.VolumeLoader(container);

  if (filename.endsWith('.mhd') && rawExtension.length == 0) {
    const nameWithoutExtension = filename.substring(0, name.lastIndexOf('.'));
    window.setWarningMessage(
      'Could not find raw file associated to mhd file: ' +
        filename +
        '.  Tried to find a file with the name ' +
        nameWithoutExtension +
        ' and extensions .raw, .zraw and .raw.gz.');
    return;
  }

  // Adapt to double files for mhd/raw format.  Many images can be
  // loaded in a single call to the loader (feature not used here).
  // So the loader accepts either a single url, or a table of urls.
  // In the case of mhd/raw, the single url must be replaced by a
  // table with the 2 urls.  But to not make ami think that these are
  // 2 urls for 2 different images, this table must be included in the
  // main table listing all images.  Hence the 2 imbricated tables in
  // the following expression.
  const urlsToLoad = filename.endsWith('.mhd')
        ? [ [ url, url.replace(/\.mhd$/, rawExtension).replace(/\.mhd&/, rawExtension + "&") ] ]
        : url;

  loader
    .load(urlsToLoad)
    .then(function(e) {
      const series = loader.data[0].mergeSeries(loader.data);
      const stack = series[0].stack[0];
      loader.free();

      const stackHelper = new window.AMI.StackHelper(stack);
      stackHelper.bbox.visible = false;
      stackHelper.border.color = colors.red;
      scene.add(stackHelper);

      gui(stackHelper);

      // center camera and interactor to center of bouding box
      // for nicer experience
      // set camera
      const worldbb = stack.worldBoundingBox();
      const lpsDims = new window.THREE.Vector3(
        worldbb[1] - worldbb[0],
        worldbb[3] - worldbb[2],
        worldbb[5] - worldbb[4]
      );

      const box = {
        center: stack.worldCenter().clone(),
        halfDimensions: new window.THREE.Vector3(
          lpsDims.x + 10, lpsDims.y + 10, lpsDims.z + 10),
      };

      // init and zoom
      const canvas = {
        width: container.clientWidth,
        height: container.clientHeight,
      };

      camera.directions = [stack.xCosine, stack.yCosine, stack.zCosine];
      camera.box = box;
      camera.canvas = canvas;
      camera.update();
      camera.fitBox(2);

      window.hideMessage();
    })
    .catch(function(error) {
      window.console.log('Error loading ' + filename, error);
      window.setWarningMessage('Error loading ' + filename + '<br />' + error);
    });

  const animate = function() {
    controls.update();
    renderer.render(scene, camera);

    requestAnimationFrame(function() {
      animate();
    });
  };

  animate();

  const gui = function(stackHelper) {
    const gui = new window.dat.GUI({
      autoPlace: false,
    });

    const customContainer = document.getElementById('gui_' + divId);
    customContainer.appendChild(gui.domElement);
    const camUtils = {
      invertRows: false,
      invertColumns: false,
      rotate45: false,
      rotate: 0,
      orientation: 'default',
      convention: 'radio',
    };

    // camera
    const cameraFolder = gui.addFolder('Camera');
    const invertRows = cameraFolder.add(camUtils, 'invertRows');
    invertRows.onChange(function() {
      camera.invertRows();
    });

    const invertColumns = cameraFolder.add(camUtils, 'invertColumns');
    invertColumns.onChange(function() {
      camera.invertColumns();
    });

    const rotate45 = cameraFolder.add(camUtils, 'rotate45');
    rotate45.onChange(function() {
      camera.rotate();
    });

    cameraFolder
      .add(camera, 'angle', 0, 360)
      .step(1)
      .listen();

    const orientationUpdate = cameraFolder.add(camUtils, 'orientation', [
      'default',
      'axial',
      'coronal',
      'sagittal',
    ]);
    orientationUpdate.onChange(function(value) {
      camera.orientation = value;
      camera.update();
      camera.fitBox(2);
      stackHelper.orientation = camera.stackOrientation;
    });

    const conventionUpdate =
          cameraFolder.add(camUtils, 'convention', ['radio', 'neuro']);
    conventionUpdate.onChange(function(value) {
      camera.convention = value;
      camera.update();
      camera.fitBox(2);
    });

    cameraFolder.open();

    const stackFolder = gui.addFolder('Stack');
    stackFolder
      .add(stackHelper, 'index', 0, stackHelper.stack.dimensionsIJK.z - 1)
      .step(1)
      .listen();
    stackFolder
      .add(stackHelper.slice, 'interpolation', 0, 1)
      .step(1)
      .listen();
    stackFolder.open();
  }

  let timeoutId = null;
  function amiViewerResizeCanvas() {
    if (timeoutId != null)
      clearTimeout(timeoutId);

    // To coalesce multiple resize events happenning in less than
    // 200ms.
    timeoutId = setTimeout(function() {
      timeoutId = null;
      onWindowResize();
    }, 200);
  }

  return {
    amiViewerResizeCanvas
  };
}

function amiViewerResizeCanvas(amiViewer) {
  amiViewer.amiViewerResizeCanvas();
}
