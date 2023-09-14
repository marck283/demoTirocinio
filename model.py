from diffusers import DiffusionPipeline
import torch
import sys

pipeline = DiffusionPipeline.from_pretrained(
    "runwayml/stable-diffusion-v1-5",
    revision="fp16",
    torch_dtype=torch.float16)
pipeline.to("cuda")

text = sys.argv[1]  # Testo da cui generare l'immagine
width = int(sys.argv[5])
height = int(sys.argv[6])

image = pipeline(text, width, height).images[0]

iname = sys.argv[2]
print(len(iname))
if len(iname) < 3:
    missing = 3 - len(iname)
    iname = iname.rjust(len(iname) + missing, '0')

pathToImagesFolder = sys.argv[4]
image.save(pathToImagesFolder + "/" + iname + f"." + sys.argv[3])
