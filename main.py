from diffusers import DiffusionPipeline
import torch

# Questo modello usa StableDiffusion (https://huggingface.co/blog/stable_diffusion)
pipeline = DiffusionPipeline.from_pretrained("runwayml/stable-diffusion-v1-5", torch_dtype=torch.float16)
pipeline.to("cuda")
image = pipeline("An image of a dog with a red necklace.", height=1080, width=1920).images[0]
image.save(f"pretrained_dog.png")
