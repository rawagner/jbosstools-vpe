/**
 * Created by JetBrains WebStorm.
 * User: yradtsevich
 * Date: 11.4.12
 * Time: 18.32
 * To change this template use File | Settings | File Templates.
 */
// Get the CanvasPixelArray from the given coordinates and dimensions.
function main() {
    var img = new Image();
//    img.src = 'orig.png';
    var canvas = document.getElementById("canvasId");
    var context = canvas.getContext("2d");
    context.fillText("Drop an image here", 240, 200);
    context.fillText("The visible area of the image must touch the top image border", 240, 240);
    context.fillText("(i.e. the first row of the image must have at least 1 filled pixel).", 240, 255);

    canvas.addEventListener("drop", function (evt) {
        var files = evt.dataTransfer.files;
        if (files.length > 0) {
            var file = files[0];
            if (typeof FileReader !== "undefined" && file.type.indexOf("image") != -1) {
                var reader = new FileReader();
                // Note: addEventListener doesn't work in Google Chrome for this event
                reader.onload = function (evt) {
                    img.src = evt.target.result;
                };
                reader.readAsDataURL(file);
            }
        }
        evt.preventDefault();
    }, false);

    img.onload = function() {
        canvas.width = img.width;
        canvas.height = img.height;
        context.drawImage(img, 0, 0);
        var imgd = context.getImageData(0, 0, img.width, img.height);
        var pix = imgd.data;

        var fullPath = findPath(img.width, img.height, function(point) {
            if (point.x < 0 || point.x >= img.width
                ||point.y < 0 || point.y >= img.height) {
                return true;
            }
            var alphaP = pix[(point.y * img.width + point.x) * 4 + 3];
            return alphaP == 0;
        });

        var packedPath = packPath(fullPath);
        var output = document.getElementById("outputId");
        output.innerHTML = "<B>int[] path = {</B>";
        for (var i = 0; i < packedPath.length - 1; i++) {
            output.innerHTML += packedPath[i].x + ', ' + packedPath[i].y + ', ';
        }
        var lastIndex = packedPath.length - 1;
        output.innerHTML += packedPath[lastIndex].x + ', ' + packedPath[lastIndex].y + "<B>};</B>";
    }

    var findPath = function(width, height, isEmpty) {
        var directions = [{x : 1, y : 0}, {x : 1, y : 1}, {x : 0, y : 1}, {x : -1, y : 1}, {x : -1, y : 0}, {x : -1, y : -1}, {x : 0, y : -1}, {x : 1, y : -1}];
        for (var i = 0; i < directions.length; i++) {
            directions[i].next = directions[(i + 1) % directions.length];
            directions[i].opposite = directions[(i + directions.length / 2) % directions.length];
        }

        var initialDirection = directions[0];
        var startPoint = {x:0, y:0};
        var point = startPoint;
        while (isEmpty(point)) {
            point.x += initialDirection.x;
            point.y += initialDirection.y;
        }

        var path = [point];
        var direction = initialDirection;

        do {
            direction = direction.opposite;
            do {
                var newPoint = {};
                direction = direction.next;
                newPoint.x = point.x + direction.x;
                newPoint.y = point.y + direction.y;
            } while (isEmpty(newPoint));
            point = newPoint;
            path.push(point);
        } while (startPoint.x != point.x || startPoint.y != point.y);

        return path;
    }

    var packPath = function(path) {
        var packed = [path[0]];
        for (var i = 1; i < path.length - 1; i++) {
            if (((path[i].x - path[i - 1].x) != (path[i + 1].x - path[i].x))
                ||((path[i].y - path[i - 1].y) != (path[i + 1].y - path[i].y))) {
                packed.push(path[i]);
            }
        }

        return packed;
    }
}
